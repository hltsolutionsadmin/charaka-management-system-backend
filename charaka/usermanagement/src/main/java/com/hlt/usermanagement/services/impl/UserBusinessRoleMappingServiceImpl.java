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
    private final UserRepository userRepository;
    private final B2BUnitRepository b2bRepository;
    private final RoleRepository roleRepository;
    private final UserBusinessRoleMappingPopulator populator;

    @Override
    @Transactional
    public UserBusinessRoleMappingDTO onboardHospitalAdmin(UserBusinessRoleMappingDTO dto) {
        UserModel user = createUserFromDTO(dto.getUserDetails(), dto.getBusinessId());
        UserBusinessRoleMappingModel mapping = createMapping(user, ERole.ROLE_HOSPITAL_ADMIN);
        mappingRepository.save(mapping);

        UserBusinessRoleMappingDTO response = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, response);
        return response;
    }

    @Override
    @Transactional
    public void onboardDoctor(UserBusinessRoleMappingDTO dto) {
        UserModel user = fetchOrCreateUser(dto);
        validateDuplicateMapping(user.getId(), dto.getBusinessId(), ERole.ROLE_DOCTOR);
        mappingRepository.save(createMapping(user, ERole.ROLE_DOCTOR));
    }

    @Override
    @Transactional
    public void onboardReceptionist(UserBusinessRoleMappingDTO dto) {
        UserModel user = fetchOrCreateUser(dto);
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

//        if (mappingRepository.existsByUserIdAndB2bUnitAndRoleAndIsActiveTrue(user.getId(), hospitalId, ERole.ROLE_TELECALLER)) {
//            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, "Telecaller already assigned to this hospital");
//        }

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
//        if (mappingRepository.existsByUserIdAndB2BUnitIdAndRole(userId, hospitalId, role)) {
//            throw new HltCustomerException(ErrorCode.ALREADY_EXISTS, role.name() + " already mapped to this hospital");
//        }
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
        user.setJuviId(UUID.randomUUID().toString());

        RoleModel defaultRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));

        user.setRoleModels(Set.of(defaultRole));

        String plainPassword = PasswordUtil.generateRandomPassword(8);
        user.setPassword(plainPassword);

        user.setB2bUnit(b2bRepository.findById(businessId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND)));

        UserModel savedUser = userRepository.save(user);

        // TODO: Email credentials securely to user
        // emailService.sendUserCredentials(user.getEmail(), user.getUsername(), plainPassword);

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
