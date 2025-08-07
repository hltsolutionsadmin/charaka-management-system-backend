package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.*;
import com.hlt.usermanagement.populator.UserBusinessRoleMappingPopulator;
import com.hlt.usermanagement.repository.*;
import com.hlt.usermanagement.services.EmailService;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.usermanagement.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBusinessRoleMappingServiceImpl implements UserBusinessRoleMappingService {

    private static final int MAX_HOSPITALS_PER_TELECALLER = 2;

    private final UserBusinessRoleMappingRepository mappingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final B2BUnitRepository b2bRepository;
    private final RoleRepository roleRepository;
    private final UserBusinessRoleMappingPopulator populator;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardHospitalAdmin(UserBusinessRoleMappingDTO dto) {
        Long businessId = dto.getBusinessId();

        // 1. Prevent duplicate hospital admin for same business
        boolean exists = mappingRepository.existsByB2bUnitIdAndRole(businessId, ERole.ROLE_HOSPITAL_ADMIN);
        if (exists) {
            throw new HltCustomerException(ErrorCode.HOSPITAL_ADMIN_ALREADY_EXISTS);
        }

        // 2. Create the user object from DTO
        UserModel user = createUserFromDTO(dto.getUserDetails(), businessId);

        // 3. Assign default role: ROLE_HOSPITAL_ADMIN
        RoleModel role = roleRepository.findByName(ERole.ROLE_HOSPITAL_ADMIN)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(List.of(role)));

        // 4. Save user before mapping (optional if cascade is set properly)
        userRepository.save(user);

        // 5. Create the business-role mapping
        UserBusinessRoleMappingModel mapping = createMapping(user, ERole.ROLE_HOSPITAL_ADMIN);
        mappingRepository.save(mapping);

        // 6. Convert to DTO and return
        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);
        return response;
    }


    @Override
    @Transactional
    public void onboardDoctor(UserBusinessRoleMappingDTO dto) {
        UserModel user = fetchOrCreateUser(dto);
        RoleModel role = roleRepository.findByName(ERole.ROLE_DOCTOR)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(List.of(role)));

        // 4. Save user before mapping (optional if cascade is set properly)
        userRepository.save(user);
        validateDuplicateMapping(user.getId(), dto.getBusinessId(), ERole.ROLE_DOCTOR);
        mappingRepository.save(createMapping(user, ERole.ROLE_DOCTOR));
    }

    @Override
    @Transactional
    public void onboardTelecaller(UserBusinessRoleMappingDTO dto) {
        UserModel user = fetchOrCreateUser(dto);
        RoleModel role = roleRepository.findByName(ERole.ROLE_TELECALLER)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(List.of(role)));
        userRepository.save(user);
        mappingRepository.save(createMapping(user, ERole.ROLE_DOCTOR));
    }

    @Override
    @Transactional
    public void onboardReceptionist(UserBusinessRoleMappingDTO dto) {
        UserModel user = fetchOrCreateUser(dto);
        RoleModel role = roleRepository.findByName(ERole.ROLE_RECEPTIONIST)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(List.of(role)));
        userRepository.save(user);
        validateDuplicateMapping(user.getId(), dto.getBusinessId(), ERole.ROLE_RECEPTIONIST);
        mappingRepository.save(createMapping(user, ERole.ROLE_RECEPTIONIST));
    }

    @Override
    @Transactional
    public void assignTelecallerToHospital(Long telecallerId, Long hospitalId) {
        UserModel user;

        if (telecallerId != null) {
            user = getUserOrThrow(telecallerId);
        } else {
            throw new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller ID is required for assignment");
        }

        if (mappingRepository.countByUserIdAndRoleAndIsActiveTrue(user.getId(), ERole.ROLE_TELECALLER) >= MAX_HOSPITALS_PER_TELECALLER) {
            throw new HltCustomerException(ErrorCode.INVALID_ROLE_FOR_OPERATION, "Telecaller is already assigned to 2 hospitals");
        }

        if (mappingRepository.existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(user.getId(), hospitalId, ERole.ROLE_TELECALLER)) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, "Telecaller already assigned to this hospital");
        }

        user.setB2bUnit(b2bRepository.findById(hospitalId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND)));

        mappingRepository.save(createMapping(user, ERole.ROLE_TELECALLER));
    }

    @Override
    public List<UserDTO> getAssignableTelecallersForHospital(Long hospitalId) {
        // TODO: Implement in UserRepository: findTelecallersWithLessThan2Hospitals
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<UserDTO> getDoctorsByHospital(Long hospitalId) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_DOCTOR);
    }

    @Override
    public List<UserDTO> getReceptionistsByHospital(Long hospitalId) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_RECEPTIONIST);
    }

    // ---------- PRIVATE HELPERS ----------

    private UserModel getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private void validateDuplicateMapping(Long userId, Long hospitalId, ERole role) {
        if (mappingRepository.existsByUserIdAndB2bUnit_IdAndRoleAndIsActiveTrue(userId, hospitalId, role)) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, role.name() + " already mapped to this hospital");
        }
    }

    private UserBusinessRoleMappingModel createMapping(UserModel user, ERole role) {
        return UserBusinessRoleMappingModel.builder()
                .user(user)
                .b2bUnit(user.getB2bUnit())
                .role(role)
                .isActive(true)
                .build();
    }

    private List<UserDTO> getUsersByRoleAndHospital(Long hospitalId, ERole role) {
//        return mappingRepository.findByB2BUnitIdAndRole(hospitalId, role)
//                .stream()
//                .map(this::toUserDTO)
//                .collect(Collectors.toList());
        return null;
    }

    private UserModel fetchOrCreateUser(UserBusinessRoleMappingDTO dto) {
        UserDTO userDTO = dto.getUserDetails();
        Long userId = userDTO.getId();

        if (userId != null) {
            return getUserOrThrow(userId);
        }

        return createUserFromDTO(userDTO, dto.getBusinessId());
    }

    private UserModel createUserFromDTO(UserDTO dto, Long businessId) {
        UserModel user = new UserModel();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setGender(dto.getGender());

        RoleModel defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(Set.of(defaultRole));

        String plainPassword = PasswordUtil.generateRandomPassword(8);
        user.setPassword(plainPassword);

        B2BUnitModel business = b2bRepository.findById(businessId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));
        user.setB2bUnit(business);

        UserModel savedUser;
        try {
            savedUser = userService.saveUser(user);
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, "User with this email or contact already exists");
        }

        // Email credentials using the generic method
        Map<String, Object> emailContext = new HashMap<>();
        emailContext.put("username", savedUser.getUsername());
        emailContext.put("password", plainPassword);
        emailContext.put("businessName", business.getBusinessName());
        emailContext.put("roles", savedUser.getRoleModels()
                .stream()
                .map(RoleModel::getName)
                .map(Enum::name)
                .collect(Collectors.toSet()));

        emailService.sendEmail(
                savedUser.getEmail(),
                "Your Account Credentials",
                "user-credentials.html",
                emailContext
        );

        return savedUser;
    }


    private UserDTO toUserDTO(UserModel user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .primaryContact(user.getPrimaryContact())
                .email(user.getEmail())
                .build();
    }
}
