package com.hlt.usermanagement.services.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.populator.UserBusinessRoleMappingPopulator;
import com.hlt.usermanagement.repository.B2BUnitRepository;
import com.hlt.usermanagement.repository.UserBusinessRoleMappingRepository;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBusinessRoleMappingServiceImpl implements UserBusinessRoleMappingService {

    private final UserRepository userRepository;
    private final B2BUnitRepository b2bUnitRepository;
    private final UserBusinessRoleMappingRepository mappingRepository;
    private final UserBusinessRoleMappingPopulator populator;

    @Override
    public UserBusinessRoleMappingDTO assignUserToBusiness(Long userId, Long b2bUnitId, String roleStr) {
        ERole role = parseRole(roleStr);

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));

        B2BUnitModel b2bUnit = b2bUnitRepository.findById(b2bUnitId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));

        validateRoleAssignment(userId, b2bUnitId, role);

        UserBusinessRoleMappingModel model = UserBusinessRoleMappingModel.builder()
                .user(user)
                .b2bUnit(b2bUnit)
                .role(role)
                .isActive(true)
                .build();

        UserBusinessRoleMappingModel saved = mappingRepository.save(model);
        UserBusinessRoleMappingDTO dto = new UserBusinessRoleMappingDTO();
        populator.populate(saved, dto);
        return dto;
    }

    private ERole parseRole(String roleStr) {
        try {
            return ERole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new HltCustomerException(ErrorCode.INVALID_ROLE);
        }
    }

    private void validateRoleAssignment(Long userId, Long b2bUnitId, ERole role) {
        if (role == ERole.ROLE_TELECALLER) {
            long telecallerCount = mappingRepository.countByUserIdAndRoleAndIsActiveTrue(userId, role);
            if (telecallerCount >= 2) {
                throw new HltCustomerException(ErrorCode.TELECALLER_LIMIT_EXCEEDED);
            }
        }

        boolean exists = mappingRepository.existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(userId, b2bUnitId, role);
        if (exists) {
            throw new HltCustomerException(ErrorCode.DUPLICATE_MAPPING);
        }
    }

    @Override
    public List<UserBusinessRoleMappingDTO> getUserRoles(Long userId) {
        return mappingRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(mapping -> {
                    UserBusinessRoleMappingDTO dto = new UserBusinessRoleMappingDTO();
                    populator.populate(mapping, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateMapping(Long mappingId) {
        UserBusinessRoleMappingModel model = mappingRepository.findById(mappingId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.MAPPING_NOT_FOUND));
        model.setActive(false);
        mappingRepository.save(model);
    }
}
