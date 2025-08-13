package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.MailRequestDTO;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.dto.enums.EmailType;
import com.hlt.usermanagement.model.B2BUnitModel;
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
import org.apache.commons.codec.digest.DigestUtils;
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

    private static final ERole TELECALLER_ROLE = ERole.ROLE_TELECALLER;

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
        sendOnboardingEmail(user.getEmail(), user.getUsername(), user.getFullName(), user.getPassword(), ERole.ROLE_HOSPITAL_ADMIN);
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
        validateUserNotExists(userDTO.getUsername());

        UserModel user = createUserWithoutBusiness(userDTO);
        assignRolesToUser(user, ERole.ROLE_TELECALLER);
        userRepository.save(user);

        UserBusinessRoleMappingModel mapping = UserBusinessRoleMappingModel.builder()
                .user(user)
                .role(ERole.ROLE_TELECALLER)
                .isActive(true)
                .build();
        mapping = mappingRepository.save(mapping);
        sendOnboardingEmail(userDTO.getEmail(), userDTO.getUsername(), userDTO.getFullName(), user.getPassword(), ERole.ROLE_TELECALLER);
        return populateResponse(mapping);
    }


    private void validateUserNotExists(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS);
        });
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
    public UserBusinessRoleMappingDTO assignTelecallerToHospital(Long telecallerMappingId, Long hospitalId) {

        if (telecallerMappingId == null) {
            throw new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller mapping ID is required");
        }

        // Fetch the telecaller mapping
        UserBusinessRoleMappingModel existingMapping = mappingRepository.findById(telecallerMappingId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller mapping not found"));

        UserModel telecaller = existingMapping.getUser();

        // 1. Check max allowed hospitals for this telecaller
        long activeAssignments = mappingRepository.countByUserIdAndRoleAndIsActiveTrue(
                telecaller.getId(), ERole.ROLE_TELECALLER
        );
        if (activeAssignments >= MAX_HOSPITALS_PER_TELECALLER) {
            throw new HltCustomerException(ErrorCode.INVALID_ROLE_FOR_OPERATION,
                    "Telecaller already assigned to the maximum number of hospitals (" + MAX_HOSPITALS_PER_TELECALLER + ")");
        }

        // 2. Prevent duplicate assignment to the same hospital
        boolean alreadyAssigned = mappingRepository.existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(
                telecaller.getId(), hospitalId, ERole.ROLE_TELECALLER
        );
        if (alreadyAssigned) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, "Telecaller already assigned to this hospital");
        }

        // 3. Fetch the hospital
        B2BUnitModel hospital = b2bRepository.findById(hospitalId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        // 4. Update telecaller's business mapping
        Set<B2BUnitModel> assignedBusinesses = Optional.ofNullable(telecaller.getBusinesses())
                .orElse(new HashSet<>());
        assignedBusinesses.add(hospital);
        telecaller.setBusinesses(assignedBusinesses);
        userRepository.save(telecaller);

        // 5. Create and save the new mapping
        UserBusinessRoleMappingModel newMapping = saveMapping(telecaller, ERole.ROLE_TELECALLER);
        newMapping.setB2bUnit(hospital); // ensure hospital is linked
        mappingRepository.save(newMapping);

        // 6. Return DTO
        return populateResponse(newMapping);
    }



    private UserBusinessRoleMappingModel saveMapping(UserModel user, ERole role, B2BUnitModel hospital) {
        UserBusinessRoleMappingModel mapping = new UserBusinessRoleMappingModel();
        mapping.setUser(user);
        mapping.setB2bUnit(hospital);
        mapping.setRole(role);
        return mappingRepository.save(mapping);
    }



    @Override
    public Page<UserDTO> getAssignableTelecallersForHospital(Long hospitalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<UserModel> userPage = mappingRepository
                .findTelecallersAssignableToHospital(ERole.ROLE_TELECALLER, hospitalId, pageable);

        List<UserDTO> assignable = userPage
                .stream()
                .map(this::toUserDTO)
                .toList();
        return new PageImpl<>(assignable, pageable, userPage.getTotalElements());
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

        B2BUnitModel business = user.getBusinesses().stream()
                .findFirst()
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "User has no assigned business"));

        return mappingRepository.save(UserBusinessRoleMappingModel.builder()
                .user(user)
                .b2bUnit(business)
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
        user.setEmailHash(DigestUtils.sha256Hex(dto.getEmail()));
        user.setPrimaryContactHash(DigestUtils.sha256Hex(dto.getPrimaryContact()));
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setGender(dto.getGender());
        assignRolesToUser(user, ERole.ROLE_USER);
        user.setPassword(generateRandomPassword(8));
        B2BUnitModel business = b2bRepository.findById(businessId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        Set<B2BUnitModel> businesses = new HashSet<>();
        businesses.add(business);
        business.setOwner(user);
        user.setBusinesses(businesses);

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
        user.setEmailHash(DigestUtils.sha256Hex(dto.getEmail()));
        user.setPrimaryContactHash(DigestUtils.sha256Hex(dto.getPrimaryContact()));
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