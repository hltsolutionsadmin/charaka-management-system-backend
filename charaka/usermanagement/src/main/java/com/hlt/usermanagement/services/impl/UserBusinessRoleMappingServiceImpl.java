package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.MailRequestDTO;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.enums.EmailType;
import com.hlt.usermanagement.model.*;
import com.hlt.usermanagement.populator.UserBusinessRoleMappingPopulator;
import com.hlt.usermanagement.repository.B2BUnitRepository;
import com.hlt.usermanagement.repository.RoleRepository;
import com.hlt.usermanagement.repository.UserBusinessRoleMappingRepository;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.EmailService;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.usermanagement.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.hlt.usermanagement.utils.PasswordUtil.generateRandomPassword;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBusinessRoleMappingServiceImpl implements UserBusinessRoleMappingService {

    private static final ERole TELECALLER_ROLE = ERole.ROLE_TELECALLER;

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

        if (mappingRepository.existsByB2bUnitIdAndRole(dto.getBusinessId(), ERole.ROLE_HOSPITAL_ADMIN)) {
            throw new HltCustomerException(ErrorCode.HOSPITAL_ADMIN_ALREADY_EXISTS);
        }

        UserModel user = fetchOrCreateUser(dto);
        assignRolesToUser(user, ERole.ROLE_HOSPITAL_ADMIN);
        userRepository.save(user);

        sendOnboardingEmail(user, ERole.ROLE_HOSPITAL_ADMIN);

        UserBusinessRoleMappingModel mapping = saveMapping(user, ERole.ROLE_HOSPITAL_ADMIN);
        return populateResponse(mapping);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardDoctor(UserBusinessRoleMappingDTO dto) {
        return onboardGenericRole(dto, ERole.ROLE_DOCTOR);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardReceptionist(UserBusinessRoleMappingDTO dto) {
        return onboardGenericRole(dto, ERole.ROLE_RECEPTIONIST);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardTelecaller(UserBusinessRoleMappingDTO dto) {
        UserDTO userDTO = dto.getUserDetails();
        validateUserNotExists(userDTO.getUsername());

        UserModel user = createUserWithoutBusiness(userDTO);
        assignRolesToUser(user, TELECALLER_ROLE);
        userRepository.save(user);

        UserBusinessRoleMappingModel mapping = UserBusinessRoleMappingModel.builder()
                .user(user)
                .role(ERole.ROLE_TELECALLER)
                .isActive(true)
                .build();
        mapping = mappingRepository.save(mapping);
        sendOnboardingEmail(user, TELECALLER_ROLE);

        return populateResponse(mapping);
    }
    private void sendOnboardingEmail(UserModel user, ERole role) {
        String subject = switch (role) {
            case ROLE_HOSPITAL_ADMIN -> "Welcome to Charaka - Hospital Admin Access";
            case ROLE_DOCTOR -> "Welcome to Charaka - Doctor Access";
            case ROLE_RECEPTIONIST -> "Welcome to Charaka - Receptionist Access";
            case ROLE_TELECALLER -> "Welcome to Charaka - Telecaller Access";
            default -> "Welcome to Charaka - Account Access";
        };

        EmailType emailType = switch (role) {
            case ROLE_HOSPITAL_ADMIN -> EmailType.HOSPITAL_ADMIN_ONBOARD;
            case ROLE_DOCTOR -> EmailType.DOCTOR_ONBOARD;
            case ROLE_RECEPTIONIST -> EmailType.RECEPTIONIST_ACCESS;
            case ROLE_TELECALLER -> EmailType.TELECALLER_ACCESS;
            default -> throw new IllegalArgumentException("No EmailType mapping for role: " + role);
        };

        MailRequestDTO mail = MailRequestDTO.builder()
                .to(user.getEmail())
                .subject(subject)
                .type(emailType)
                .variables(Map.of(
                        "name", user.getFullName(),
                        "username", user.getUsername(),
                        "password", user.getPassword()
                ))
                .build();

        emailService.sendMail(mail);
    }
    private UserBusinessRoleMappingDTO onboardGenericRole(UserBusinessRoleMappingDTO dto, ERole role) {
        UserModel user = fetchOrCreateUser(dto);
        assignRolesToUser(user, role);
        userRepository.save(user);
        validateDuplicateMapping(user.getId(), dto.getBusinessId(), role);

        UserBusinessRoleMappingModel mapping = saveMapping(user, role);
        sendOnboardingEmail(user, role);

        return populateResponse(mapping);
    }

    private UserModel fetchOrCreateUser(UserBusinessRoleMappingDTO dto) {
        UserDTO userDTO = dto.getUserDetails();
        return (userDTO.getId() != null) ? getUserOrThrow(userDTO.getId()) : createUserFromDTO(userDTO, dto.getBusinessId());
    }

    private UserModel getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private void validateDuplicateMapping(Long userId, Long hospitalId, ERole role) {
        if (mappingRepository.existsByUserIdAndB2bUnit_IdAndRoleAndIsActiveTrue(userId, hospitalId, role)) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, role.name() + " already mapped to this hospital");
        }
    }

    private void validateUserNotExists(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS);
        });
    }
    private UserModel createUserFromDTO(UserDTO dto, Long businessId) {
        UserModel user = new UserModel();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setEmailHash(hash(dto.getEmail()));
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setPrimaryContactHash(hash(dto.getPrimaryContact()));
        user.setGender(dto.getGender());
        user.setPassword(generateRandomPassword(8));

        assignRolesToUser(user, ERole.ROLE_USER);

        B2BUnitModel business = b2bRepository.findById(businessId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));
        Set<B2BUnitModel> businesses = new HashSet<>();
        businesses.add(business);
        user.setBusinesses(businesses);

        if (dto.getAttributes() != null) {
            dto.getAttributes().forEach((key, value) -> {
                UserAttributeModel attr = new UserAttributeModel();
                attr.setAttributeName(key);
                attr.setAttributeValue(value);
                attr.setUser(user);
                user.getAttributes().add(attr);
            });
        }

        try {
            return userService.saveUser(user);
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS,
                    "User with this email or contact already exists");
        }
    }

    private UserModel createUserWithoutBusiness(UserDTO dto) {
        UserModel user = new UserModel();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setEmailHash(hash(dto.getEmail()));
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setPrimaryContactHash(hash(dto.getPrimaryContact()));
        user.setGender(dto.getGender());
        user.setPassword(generateRandomPassword(8));
        return user;
    }

    private String hash(String input) {
        return DigestUtils.sha256Hex(input);
    }
    private UserBusinessRoleMappingModel saveMapping(UserModel user, ERole role) {
        B2BUnitModel business = user.getBusinesses().stream()
                .findFirst()
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "User has no assigned business"));

        UserBusinessRoleMappingModel mapping = new UserBusinessRoleMappingModel();
        mapping.setUser(user);
        mapping.setB2bUnit(business);
        mapping.setRole(role);
        mapping.setIsActive(true);

        return mappingRepository.save(mapping);
    }

    private UserBusinessRoleMappingModel saveMapping(UserModel user, ERole role, B2BUnitModel hospital) {
        UserBusinessRoleMappingModel mapping = new UserBusinessRoleMappingModel();
        mapping.setUser(user);
        mapping.setB2bUnit(hospital);
        mapping.setRole(role);
        mapping.setIsActive(true);
        return mappingRepository.save(mapping);
    }

    private void assignRolesToUser(UserModel user, ERole role) {
        RoleModel roleModel = roleRepository.findByName(role)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(Collections.singleton(roleModel)));
    }
    private UserBusinessRoleMappingDTO populateResponse(UserBusinessRoleMappingModel mapping) {
        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);
        if (response.getUserDetails() != null) {
            response.getUserDetails().setPassword(mapping.getUser().getPassword());
        }
        return response;
    }

    private UserDTO toUserDTO(UserModel user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setPrimaryContact(user.getPrimaryContact());
        dto.setEmail(user.getEmail());
        return dto;
    }

    private Page<UserDTO> getUsersByRoleAndHospital(Long hospitalId, ERole role, Pageable pageable) {
        return mappingRepository.findByB2bUnitIdAndRole(hospitalId, role, pageable)
                .map(mapping -> toUserDTO(mapping.getUser()));
    }
    @Override
    @Transactional
    public UserBusinessRoleMappingDTO assignTelecallerToHospital(Long telecallerMappingId, Long hospitalId) {
        if (telecallerMappingId == null || hospitalId == null) {
            throw new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller ID and Hospital ID are required");
        }

        List<UserBusinessRoleMappingModel> mappings = mappingRepository.findByUserId(telecallerMappingId);
        if (mappings.isEmpty()) {
            throw new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller mapping not found");
        }
        UserModel user = mappings.get(0).getUser();
        B2BUnitModel hospital = b2bRepository.findById(hospitalId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        // Update user-business relationship
        Set<B2BUnitModel> businesses = Optional.ofNullable(user.getBusinesses()).orElse(new HashSet<>());
        businesses.add(hospital);
        user.setBusinesses(businesses);

        userRepository.save(user);

        return populateResponse(saveMapping(user, TELECALLER_ROLE, hospital));
    }

    @Override
    public Page<UserDTO> getDoctorsByHospital(Long hospitalId, Pageable pageable) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_DOCTOR, pageable);
    }

    @Override
    public Page<UserDTO> getReceptionistsByHospital(Long hospitalId, Pageable pageable) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_RECEPTIONIST, pageable);
    }

    @Override
    public Page<UserDTO> getAssignableTelecallersForHospital(Long hospitalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserModel> userPage = mappingRepository.findTelecallersAssignableToHospital(
                ERole.ROLE_TELECALLER, hospitalId, pageable
        );

        List<UserDTO> assignable = userPage.stream()
                .map(this::toUserDTO)
                .toList();

        return new PageImpl<>(assignable, pageable, userPage.getTotalElements());
    }

    @Override
    public Page<UserDTO> getPartnersByBusinessAndType(Long businessId, String type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        ERole role;
        try {
            role = ERole.valueOf("ROLE_" + type.toUpperCase());
            // Example: "doctor" → ROLE_DOCTOR
        } catch (IllegalArgumentException e) {
            throw new HltCustomerException(ErrorCode.ROLE_NOT_FOUND, "Invalid role type: " + type);
        }

        return mappingRepository.findByB2bUnitIdAndRole(businessId, role, pageable)
                .map(mapping -> toUserDTO(mapping.getUser()));
    }




}