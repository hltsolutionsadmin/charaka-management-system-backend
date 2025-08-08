package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.MailRequestDTO;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.dto.enums.EmailType;
import com.hlt.usermanagement.model.RoleModel;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import com.hlt.usermanagement.model.UserModel;
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
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final int MAX_HOSPITALS_PER_TELECALLER = 2;

    private final UserBusinessRoleMappingRepository mappingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final B2BUnitRepository b2bRepository;
    private final RoleRepository roleRepository;
    private final UserBusinessRoleMappingPopulator populator;
    @Autowired
    private EmailService emailService;


    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardHospitalAdmin(UserBusinessRoleMappingDTO dto) {
        Long businessId = dto.getBusinessId();
        if (mappingRepository.existsByB2bUnitIdAndRole(businessId, ERole.ROLE_HOSPITAL_ADMIN)) {
            throw new HltCustomerException(ErrorCode.HOSPITAL_ADMIN_ALREADY_EXISTS);
        }

        UserModel user = createUserFromDTO(dto.getUserDetails(), businessId);
        assignRolesToUser(user, ERole.ROLE_HOSPITAL_ADMIN);
        userRepository.save(user);
        sendOnboardingEmail(user.getEmail(),user.getUsername() ,user.getFullName(),user.getPassword(), ERole.ROLE_HOSPITAL_ADMIN);
        UserBusinessRoleMappingModel mapping = saveMapping(user, ERole.ROLE_HOSPITAL_ADMIN);
        return populateResponse(mapping);
    }
    @Transactional
    private void sendOnboardingEmail(String email, String username, String fullName, String password, ERole role) {
        String subject = getSubjectForRole(role);
        EmailType emailType = getEmailTypeForRole(role);

        MailRequestDTO mail = MailRequestDTO.builder()
                .to(email)
                .subject(subject)
                .type(emailType)
                .variables(Map.of(
                        "name", fullName,
                        "username", username,
                        "password", password
                ))
                .build();

        emailService.sendMail(mail);
    }

    private String getSubjectForRole(ERole role) {
        return switch (role) {
            case ROLE_HOSPITAL_ADMIN -> "Welcome to Charaka - Hospital Admin Access";
            case ROLE_DOCTOR -> "Welcome to Charaka - Doctor Access";
            case ROLE_RECEPTIONIST -> "Welcome to Charaka - Receptionist Access";
            case ROLE_TELECALLER -> "Welcome to Charaka - Telecaller Access";
            default -> "Welcome to Charaka - Account Access";
        };
    }

    private EmailType getEmailTypeForRole(ERole role) {
        return switch (role) {
            case ROLE_HOSPITAL_ADMIN -> EmailType.HOSPITAL_ADMIN_ONBOARD;
            case ROLE_DOCTOR -> EmailType.DOCTOR_ONBOARD;
            case ROLE_RECEPTIONIST -> EmailType.RECEPTIONIST_ACCESS;
            case ROLE_TELECALLER -> EmailType.TELECALLER_ACCESS;
            default -> throw new IllegalArgumentException("No EmailType mapping for role: " + role);
        };
    }



    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardDoctor(UserBusinessRoleMappingDTO dto) {
        sendOnboardingEmail(dto.getUserDetails().getEmail(),
                dto.getUserDetails().getUsername(),
                dto.getUserDetails().getFullName(),
                dto.getUserDetails().getPassword(),
                ERole.ROLE_DOCTOR);
        return onboardGenericRole(dto, ERole.ROLE_DOCTOR);

    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardTelecaller(UserBusinessRoleMappingDTO dto) {
        UserDTO userDTO = dto.getUserDetails();
        userRepository.findByUsername(userDTO.getUsername()).ifPresent(user -> {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS);
        });

        UserModel user = createUserWithoutBusiness(userDTO);
        assignRolesToUser(user, ERole.ROLE_TELECALLER);
        userRepository.save(user);
        sendOnboardingEmail(dto.getUserDetails().getEmail(),
                dto.getUserDetails().getUsername(),
                dto.getUserDetails().getFullName(),
                dto.getUserDetails().getPassword(),
                ERole.ROLE_TELECALLER);
        UserBusinessRoleMappingModel mapping = saveMapping(user, ERole.ROLE_TELECALLER);
        return populateResponse(mapping);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardReceptionist(UserBusinessRoleMappingDTO dto) {
        sendOnboardingEmail(dto.getUserDetails().getEmail(),
                dto.getUserDetails().getUsername(),
                dto.getUserDetails().getFullName(),
                dto.getUserDetails().getPassword(),
                ERole.ROLE_RECEPTIONIST);
        return onboardGenericRole(dto, ERole.ROLE_RECEPTIONIST);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO assignTelecallerToHospital(Long telecallerId, Long hospitalId) {
        if (telecallerId == null) throw new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller ID is required");

        UserModel user = getUserOrThrow(telecallerId);

        if (mappingRepository.countByUserIdAndRoleAndIsActiveTrue(user.getId(), ERole.ROLE_TELECALLER) >= MAX_HOSPITALS_PER_TELECALLER) {
            throw new HltCustomerException(ErrorCode.INVALID_ROLE_FOR_OPERATION, "Telecaller already assigned to 2 hospitals");
        }

        if (mappingRepository.existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(user.getId(), hospitalId, ERole.ROLE_TELECALLER)) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, "Telecaller already assigned to this hospital");
        }

        user.setB2bUnit(b2bRepository.findById(hospitalId).orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND)));
        UserBusinessRoleMappingModel mapping = saveMapping(user, ERole.ROLE_TELECALLER);
        return populateResponse(mapping);
    }

    @Override
    public Page<UserDTO> getAssignableTelecallersForHospital(Long hospitalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> userIdsPage = mappingRepository.findUserIdsWithRoleMappedToLessThanTwoHospitals(ERole.ROLE_TELECALLER, pageable);

        List<UserDTO> assignable = new ArrayList<>();
        for (Long userId : userIdsPage.getContent()) {
            if (!mappingRepository.existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(userId, hospitalId, ERole.ROLE_TELECALLER)) {
                assignable.add(toUserDTO(getUserOrThrow(userId)));
            }
        }
        return new PageImpl<>(assignable, pageable, userIdsPage.getTotalElements());
    }

    @Override
    public Page<UserDTO> getDoctorsByHospital(Long hospitalId, Pageable pageable) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_DOCTOR, pageable);
    }

    @Override
    public Page<UserDTO> getReceptionistsByHospital(Long hospitalId, Pageable pageable) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_RECEPTIONIST, pageable);
    }


    private UserBusinessRoleMappingDTO onboardGenericRole(UserBusinessRoleMappingDTO dto, ERole role) {
        UserModel user = fetchOrCreateUser(dto);
        assignRolesToUser(user, role);
        userRepository.save(user);
        validateDuplicateMapping(user.getId(), dto.getBusinessId(), role);
        UserBusinessRoleMappingModel mapping = saveMapping(user, role);
        return populateResponse(mapping);
    }

    private UserModel getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new HltCustomerException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private void validateDuplicateMapping(Long userId, Long hospitalId, ERole role) {
        if (mappingRepository.existsByUserIdAndB2bUnit_IdAndRoleAndIsActiveTrue(userId, hospitalId, role)) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, role.name() + " already mapped to this hospital");
        }
    }

    private UserBusinessRoleMappingModel saveMapping(UserModel user, ERole role) {
        return mappingRepository.save(UserBusinessRoleMappingModel.builder()
                .user(user)
                .b2bUnit(user.getB2bUnit())
                .role(role)
                .isActive(true)
                .build());
    }

    private void assignRolesToUser(UserModel user, ERole role) {
        RoleModel roleModel = roleRepository.findByName(role)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(Collections.singleton(roleModel)));
    }

    private UserModel fetchOrCreateUser(UserBusinessRoleMappingDTO dto) {
        UserDTO userDTO = dto.getUserDetails();
        if (userDTO.getId() != null) {
            return getUserOrThrow(userDTO.getId());
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
        assignRolesToUser(user, ERole.ROLE_USER);
        user.setPassword(generateRandomPassword(8));
        user.setB2bUnit(b2bRepository.findById(businessId).orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND)));

        try {
            return userService.saveUser(user);
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, "User with this email or contact already exists");
        }
    }

    private UserModel createUserWithoutBusiness(UserDTO dto) {
        UserModel user = new UserModel();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setPassword(generateRandomPassword(8));
        user.setGender(dto.getGender());
        return user;
    }

    private UserBusinessRoleMappingDTO populateResponse(UserBusinessRoleMappingModel mapping) {
        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);

        if (response.getUserDetails() != null) {
            response.getUserDetails().setPassword(mapping.getUser().getPassword());
        }
        return response;
    }


    private Page<UserDTO> getUsersByRoleAndHospital(Long hospitalId, ERole role, Pageable pageable) {
        return mappingRepository.findByB2bUnitIdAndRole(hospitalId, role, pageable)
                .map(mapping -> toUserDTO(mapping.getUser()));
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