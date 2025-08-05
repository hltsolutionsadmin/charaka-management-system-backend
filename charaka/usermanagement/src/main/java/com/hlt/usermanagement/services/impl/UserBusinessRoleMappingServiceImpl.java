package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.dto.enums.EMappingType;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.RoleModel;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.populator.UserBusinessRoleMappingPopulator;
import com.hlt.usermanagement.populator.UserPopulator;
import com.hlt.usermanagement.repository.B2BUnitRepository;
import com.hlt.usermanagement.repository.RoleRepository;
import com.hlt.usermanagement.repository.UserBusinessRoleMappingRepository;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.EmailService;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.usermanagement.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBusinessRoleMappingServiceImpl implements UserBusinessRoleMappingService {

    private final UserRepository userRepository;
    private final B2BUnitRepository b2bUnitRepository;
    private final UserBusinessRoleMappingRepository mappingRepository;
    private final UserBusinessRoleMappingPopulator populator;
    private final UserPopulator userPopulator;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Override
    public UserBusinessRoleMappingDTO assignUserToBusinessWithUserDetails(UserDTO userDTO, Long b2bUnitId, String roleStr) {
        // Step 1: Parse role
        ERole role = parseRole(roleStr);

        // Step 2: Fetch Business
        B2BUnitModel b2bUnit = b2bUnitRepository.findById(b2bUnitId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        // Step 3: Create user
        UserModel userFromDTO = createUserFromDTO(userDTO);

        // Step 4: Check telecaller mapping limit
        if (role == ERole.ROLE_TELECALLER) {
            long existingMappings = mappingRepository.countByUserIdAndRoleAndIsActiveTrue(userFromDTO.getId(), ERole.ROLE_TELECALLER);
            if (existingMappings >= 2) {
                throw new HltCustomerException(ErrorCode.TELECALLER_MAPPING_LIMIT_EXCEEDED);
            }
        }

        // Step 5: Create mapping
        UserBusinessRoleMappingModel mapping = new UserBusinessRoleMappingModel();
        mapping.setUser(userFromDTO);
        mapping.setB2bUnit(b2bUnit);
        mapping.setRole(role);
        mapping.setActive(true);
        mapping.setMappingType(getMappingTypeForRole(role));

        mappingRepository.save(mapping);

        // Step 6: Prepare response

        UserBusinessRoleMappingDTO dto = new UserBusinessRoleMappingDTO();
        populator.populate(mapping, dto);
        return dto;
    }


    @Override
    public void deactivateMapping(Long mappingId) {
        UserBusinessRoleMappingModel model = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.MAPPING_NOT_FOUND));
        model.setActive(false);
        mappingRepository.save(model);
    }

    private ERole parseRole(String roleStr) {
        try {
            return ERole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new HltCustomerException(ErrorCode.INVALID_ROLE);
        }
    }

    private EMappingType getMappingTypeForRole(ERole role) {
        return switch (role) {
            case ROLE_TELECALLER -> EMappingType.TELECALLER;
            case ROLE_HOSPITAL_ADMIN -> EMappingType.HOSPITAL_ADMIN;
            case ROLE_DOCTOR -> EMappingType.DOCTOR;
            default -> throw new HltCustomerException(ErrorCode.UNSUPPORTED_MAPPING_TYPE);
        };
    }

    private UserModel createUserFromDTO(UserDTO dto) {
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
        //TODO :Send mail
        // emailService.sendUserCredentials(user.getEmail(), user.getUsername(), plainPassword);
        emailService.sendUserCredentials(user.getEmail(), user.getUsername(), plainPassword);
        return userRepository.save(user);
    }
}
