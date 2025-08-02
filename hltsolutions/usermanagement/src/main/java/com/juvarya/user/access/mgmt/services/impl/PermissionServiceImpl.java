package com.juvarya.user.access.mgmt.services.impl;

import com.juvarya.user.access.mgmt.dto.enums.PermissionType;
import com.juvarya.user.access.mgmt.model.UserPermissionModel;
import com.juvarya.user.access.mgmt.repository.UserPermissionRepository;
import com.juvarya.user.access.mgmt.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserPermissionRepository userPermissionRepository;

    @Override
    public List<PermissionType> addPermissions(Long userId, List<PermissionType> permissions) {
        List<PermissionType> added = new ArrayList<>();

        for (PermissionType permission : permissions) {
            boolean exists = userPermissionRepository.existsByUserIdAndPermission(userId, permission);
            if (!exists) {
                userPermissionRepository.save(new UserPermissionModel(userId, permission));
                added.add(permission);
            }
        }

        return added;
    }

    @Override
    public boolean hasAccess(Long userId, String permission) {
        return userPermissionRepository.existsByUserIdAndPermission(userId, PermissionType.valueOf(permission));
    }

    @Override
    public List<PermissionType> getUserPermissions(Long userId) {
        return userPermissionRepository.findByUserId(userId)
                .stream()
                .map(UserPermissionModel::getPermission)
                .toList();
    }
}
