
package com.hlt.usermanagement.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.hlt.auth.JwtUtils;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.LoggedInUser;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.request.*;
import com.hlt.usermanagement.jwt.JwtResponse;
import com.hlt.usermanagement.model.*;
import com.hlt.usermanagement.repository.B2BUnitRepository;
import com.hlt.usermanagement.repository.UserBusinessRoleMappingRepository;
import com.hlt.usermanagement.services.RoleService;
import com.hlt.usermanagement.services.UserOTPService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.usermanagement.services.impl.UserDetailsServiceImpl;
import com.hlt.utils.JTBaseEndpoint;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController extends JTBaseEndpoint {

    private static final String SIGN_IN = "SIGNIN";
    private static final String DELIVERY = "DELIVERY";
    private static final long OTP_VALID_DURATION = 15 * 60 * 1000;

    private final UserOTPService userOTPService;
    private final UserService userService;
    private final RoleService roleService;
    private final JwtUtils jwtUtils;
    private final B2BUnitRepository b2bUnitRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    private final UserBusinessRoleMappingRepository mappingRepository;

    @PostMapping("/login")
    public ResponseEntity<Object> generateJwt(@Valid @RequestBody LoginRequest loginRequest) throws JsonProcessingException {
        log.info("Login attempt for primary contact: {}", loginRequest.getPrimaryContact());
        loginRequest.setOtpType(SIGN_IN);
        validateOtp(loginRequest);

        UserModel userModel = userDetailsService.loadUserByPrimaryContact(loginRequest.getPrimaryContact());
        if (ObjectUtils.isEmpty(userModel)) {
            userModel = registerNewUser(loginRequest);
        } else {
            log.info("Existing user found: {}", userModel.getPrimaryContact());
        }
        return ResponseEntity.ok(generateJwtResponse(userModel));
    }

    private UserModel registerNewUser(LoginRequest loginRequest) {
        log.info("Registering new user for contact: {}", loginRequest.getPrimaryContact());

        UserModel userModel = new UserModel();
        userModel.setPrimaryContact(loginRequest.getPrimaryContact());
        userModel.setFullName(loginRequest.getFullName());
        userModel.setCreationTime(new Date());
        userModel.setRecentActivityDate(LocalDate.now());
        userModel.setLastLogOutDate(LocalDate.now());

        if (StringUtils.isNotEmpty(loginRequest.getEmailAddress())) {
            userModel.setEmail(loginRequest.getEmailAddress());
        }

        // Instead of setB2bUnit(), add the business to the businesses set
        if (loginRequest.getBusinessId() != null) {
            B2BUnitModel unit = b2bUnitRepository.findById(loginRequest.getBusinessId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

            // Ensure bidirectional consistency:
            unit.setOwner(userModel);
            userModel.getBusinesses().add(unit);
        }

        Set<RoleModel> userRoles = new HashSet<>();
        userRoles.add(roleService.findByErole(ERole.ROLE_USER));
        userModel.setRoleModels(userRoles);

        userService.saveUser(userModel);
        log.info("New user registered: {}", userModel.getPrimaryContact());
        return userModel;
    }


    private void validateUserUniqueness(String username, String primaryContact, String email) {
        if (userService.findByUsername(username).isPresent()) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS);
        }
        if (userService.findByPrimaryContact(primaryContact).isPresent()) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, "Mobile number already exists");
        }
        if (StringUtils.isNotEmpty(email) && userService.findByEmail(email) != null) {
            throw new HltCustomerException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<StandardResponse<String>> registerUser(@Valid @RequestBody UsernameLoginRequest request) {
        log.info("Registering user: {}", request.getUsername());

        // 1. Validate uniqueness for username, email, contact
        validateUserUniqueness(request.getUsername(), request.getPrimaryContact(), request.getEmail());

        // 2. Build user with encrypted fields (handled by @Convert)
        UserModel newUser = new UserModel();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setEmailHash(DigestUtils.sha256Hex(request.getEmail().trim().toLowerCase()));
        newUser.setPrimaryContact(request.getPrimaryContact());
        newUser.setPrimaryContactHash(DigestUtils.sha256Hex(request.getPrimaryContact()));
        newUser.setCreationTime(new Date());
        newUser.setRecentActivityDate(LocalDate.now());
        newUser.setLastLogOutDate(LocalDate.now());

        // 3. Assign default role
        RoleModel userRole = roleService.findByErole(ERole.ROLE_USER);
        newUser.setRoleModels(Set.of(userRole));

        // 4. Save user (encryption handled by JPA layer)
        userService.saveUser(newUser);

        // 5. Return response
        return ResponseEntity.ok(StandardResponse.message("User registered successfully"));
    }


    @PostMapping("/login/username")
    public ResponseEntity<Object> loginWithUsername(@Valid @RequestBody UsernameLoginRequest request) throws JsonProcessingException {
        UserModel user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));

        if (!request.getPassword().equals(user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        user.setRecentActivityDate(LocalDate.now());
        userService.saveUser(user);

        return ResponseEntity.ok(generateJwtResponse(user));
    }


    @PostMapping("/refreshToken")
    public ResponseEntity<Object> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) throws JsonProcessingException {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (StringUtils.isNotBlank(refreshToken) && jwtUtils.validateJwtToken(refreshToken)) {
            LoggedInUser loggedInUser = jwtUtils.getUserFromToken(refreshToken);
            UserModel userModel = userService.findById(loggedInUser.getId());

            userModel.setRecentActivityDate(LocalDate.now());
            userService.saveUser(userModel);

            String newAccessToken = jwtUtils.generateJwtToken(loggedInUser);

            List<Long> businessIds = userModel.getBusinesses() != null
                    ? userModel.getBusinesses().stream()
                    .map(B2BUnitModel::getId)
                    .toList()
                    : Collections.emptyList();

            return ResponseEntity.ok(new JwtResponse(
                    newAccessToken,
                    loggedInUser.getId(),
                    loggedInUser.getPrimaryContact(),
                    loggedInUser.getEmail(),
                    new ArrayList<>(loggedInUser.getRoles()),
                    refreshToken,
                    businessIds
            ));
        }

        throw new HltCustomerException(ErrorCode.TOKEN_PROCESSING_ERROR);
    }


    @PostMapping("/verify")
    public Boolean verifyOtp(@RequestBody LoginRequest loginRequest) {
        loginRequest.setOtpType(DELIVERY);
        try {
            validateOtp(loginRequest);
            return true;
        } catch (Exception e) {
            log.warn("OTP verification failed for contact: {}", loginRequest.getPrimaryContact(), e);
            return false;
        }
    }


    private void validateOtp(LoginRequest loginRequest) {
        String otpType = loginRequest.getOtpType();
        UserOTPModel otpModel = userOTPService.findByOtpTypeAndPrimaryContact(otpType, loginRequest.getPrimaryContact());

        if (otpModel == null) {
            throw new HltCustomerException(ErrorCode.NOT_FOUND);
        }

        if (isOtpExpired(otpModel)) {
            throw new HltCustomerException(ErrorCode.OTP_EXPIRED);
        }

        if (!loginRequest.getOtp().equals(otpModel.getOtp())) {
            throw new BadCredentialsException("Invalid OTP");
        }
    }

    private boolean isOtpExpired(UserOTPModel otpModel) {
        long currentTime = System.currentTimeMillis();
        return (otpModel.getCreationTime().getTime() + OTP_VALID_DURATION) <= currentTime;
    }

    private JwtResponse generateJwtResponse(UserModel userModel) throws JsonProcessingException {
        try {
            LoggedInUser loggedInUser = convertToLoggedInUser(userModel);
            String jwt = jwtUtils.generateJwtToken(loggedInUser);
            String refreshToken = jwtUtils.generateRefreshToken(loggedInUser);

            List<Long> businessIds = userModel.getBusinesses() != null
                    ? userModel.getBusinesses().stream()
                    .map(B2BUnitModel::getId)
                    .toList()
                    : Collections.emptyList();


            List<Long> mappedBusinessIds = mappingRepository.findByUserId(userModel.getId())
                    .stream()
                    .map(UserBusinessRoleMappingModel::getB2bUnit)
                    .filter(Objects::nonNull)
                    .map(B2BUnitModel::getId)
                    .distinct()
                    .toList();

            Set<Long> allBusinessIds = new HashSet<>();
            allBusinessIds.addAll(businessIds);
            allBusinessIds.addAll(mappedBusinessIds);

            return new JwtResponse(
                    jwt,
                    loggedInUser.getId(),
                    loggedInUser.getPrimaryContact(),
                    loggedInUser.getEmail(),
                    new ArrayList<>(loggedInUser.getRoles()),
                    refreshToken,
                    new ArrayList<>(allBusinessIds)
            );
        } finally {
            userOTPService.deleteByPrimaryContactAndOtpType(userModel.getPrimaryContact(), SIGN_IN);
            log.info("Deleted OTP for contact: {}", userModel.getPrimaryContact());
        }
    }


    private LoggedInUser convertToLoggedInUser(UserModel userModel) {
        LoggedInUser user = new LoggedInUser();
        user.setId(userModel.getId());
        user.setPrimaryContact(userModel.getPrimaryContact());
        user.setEmail(userModel.getEmail());
        user.setFullName(userModel.getFullName());

        Set<String> roles = userModel.getRoleModels().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        user.setRoles(roles);

        return user;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(StandardResponse.message("Password reset  sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponse<String>> resetPassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(StandardResponse.message("Password updated successfully"));
    }
}
