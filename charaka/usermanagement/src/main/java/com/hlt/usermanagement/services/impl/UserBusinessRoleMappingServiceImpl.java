package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.*;
import com.hlt.usermanagement.populator.UserBusinessRoleMappingPopulator;
import com.hlt.usermanagement.repository.*;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.usermanagement.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

        UserBusinessRoleMappingModel mapping = createMapping(user, ERole.ROLE_HOSPITAL_ADMIN);
        mappingRepository.save(mapping);

        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);
        return response;
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardDoctor(UserBusinessRoleMappingDTO dto) {
        return onboardGenericRole(dto, ERole.ROLE_DOCTOR);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardTelecaller(UserBusinessRoleMappingDTO dto) {
        return   onboardGenericRole(dto, ERole.ROLE_TELECALLER);
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardReceptionist(UserBusinessRoleMappingDTO dto) {
        return onboardGenericRole(dto, ERole.ROLE_RECEPTIONIST);
    }

    private UserBusinessRoleMappingDTO onboardGenericRole(UserBusinessRoleMappingDTO dto, ERole role) {
        UserModel user = fetchOrCreateUser(dto);
        assignRolesToUser(user, role);
        userRepository.save(user);
        validateDuplicateMapping(user.getId(), dto.getBusinessId(), role);
        UserBusinessRoleMappingModel mapping = createMapping(user, role);
        mappingRepository.save(mapping);

        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);
        return response;
    }

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO assignTelecallerToHospital(Long telecallerId, Long hospitalId) {
        if (telecallerId == null) {
            throw new HltCustomerException(ErrorCode.NOT_FOUND, "Telecaller ID is required for assignment");
        }

        UserModel user = getUserOrThrow(telecallerId);

        if (mappingRepository.countByUserIdAndRoleAndIsActiveTrue(user.getId(), ERole.ROLE_TELECALLER) >= MAX_HOSPITALS_PER_TELECALLER) {
            throw new HltCustomerException(ErrorCode.INVALID_ROLE_FOR_OPERATION, "Telecaller is already assigned to 2 hospitals");
        }

        if (mappingRepository.existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(user.getId(), hospitalId, ERole.ROLE_TELECALLER)) {
            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, "Telecaller already assigned to this hospital");
        }

        user.setB2bUnit(b2bRepository.findById(hospitalId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND)));

        UserBusinessRoleMappingModel mapping = createMapping(user, ERole.ROLE_TELECALLER);
        mappingRepository.save(mapping);

        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);

        return response;
    }


    @Override
    public Page<UserDTO> getAssignableTelecallersForHospital(Long hospitalId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Step 1: Get paginated telecaller userIds mapped to < 2 hospitals
        Page<Long> userIdsPage = mappingRepository
                .findUserIdsWithRoleMappedToLessThanTwoHospitals(ERole.ROLE_TELECALLER, pageable);

        List<UserDTO> assignableTelecallers = new ArrayList<>();

        // Step 2: Filter out those already assigned to this hospital
        for (Long userId : userIdsPage.getContent()) {
            boolean alreadyAssigned = mappingRepository
                    .existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(userId, hospitalId, ERole.ROLE_TELECALLER);

            if (!alreadyAssigned) {
                UserModel user = userRepository.findById(userId)
                        .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
                assignableTelecallers.add(toUserDTO(user));
            }
        }

        return new PageImpl<>(assignableTelecallers, pageable, userIdsPage.getTotalElements());
    }



    @Override
    public Page<UserDTO> getDoctorsByHospital(Long hospitalId, Pageable pageable) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_DOCTOR, pageable);
    }

    @Override
    public Page<UserDTO> getReceptionistsByHospital(Long hospitalId, Pageable pageable) {
        return getUsersByRoleAndHospital(hospitalId, ERole.ROLE_RECEPTIONIST, pageable);
    }

    private Page<UserDTO> getUsersByRoleAndHospital(Long hospitalId, ERole role, Pageable pageable) {
        Page<UserBusinessRoleMappingModel> page = mappingRepository.findByB2bUnitIdAndRole(hospitalId, role, pageable);
        return page.map(mapping -> toUserDTO(mapping.getUser()));
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

    private UserBusinessRoleMappingModel createMapping(UserModel user, ERole role) {
        return UserBusinessRoleMappingModel.builder()
                .user(user)
                .b2bUnit(user.getB2bUnit())
                .role(role)
                .isActive(true)
                .build();
    }

    private void assignRolesToUser(UserModel user, ERole role) {
        RoleModel roleModel = roleRepository.findByName(role)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoleModels(new HashSet<>(Collections.singletonList(roleModel)));
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

        String plainPassword = PasswordUtil.generateRandomPassword(8);
        user.setPassword(plainPassword);

        user.setB2bUnit(b2bRepository.findById(businessId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND)));

        try {
            return userService.saveUser(user);
        } catch (Exception e) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, "User with this email or contact already exists");
        }
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
