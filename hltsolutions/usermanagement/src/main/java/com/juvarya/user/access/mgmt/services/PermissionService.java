package com.juvarya.user.access.mgmt.services;

import com.juvarya.user.access.mgmt.dto.enums.PermissionType;

import java.util.List;

public interface PermissionService {

    List<PermissionType> addPermissions(Long userId, List<PermissionType> permissions);

    boolean hasAccess(Long userId, String permission);

    List<PermissionType> getUserPermissions(Long userId);
}
