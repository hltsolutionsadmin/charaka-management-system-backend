package com.juvarya.user.access.mgmt.controllers;

import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.dto.enums.PermissionType;
import com.juvarya.user.access.mgmt.services.PermissionService;
import com.juvarya.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/user-permission")
public class UserPermissionController {

    @Autowired
    private PermissionService userPermissionService;

    @GetMapping("/has-access")
    public ResponseEntity<Boolean> hasAccess(
            @RequestParam Long userId,
            @RequestParam String permission) {
        return ResponseEntity.ok(userPermissionService.hasAccess(userId, permission));
    }

    @PostMapping("/add")
    public ResponseEntity<List<PermissionType>> addPermissions(
            @RequestBody List<String> permissions) {

        UserDetailsImpl details = SecurityUtils.getCurrentUserDetails();
        List<PermissionType> enumPermissions = permissions.stream()
                .map(PermissionType::valueOf)
                .toList();

        List<PermissionType> added = userPermissionService.addPermissions(details.getId(), enumPermissions);
        return ResponseEntity.ok(added);
    }


    @GetMapping("/list")
    public ResponseEntity<List<PermissionType>> getPermissions() {
        UserDetailsImpl details = SecurityUtils.getCurrentUserDetails();
        return ResponseEntity.ok(userPermissionService.getUserPermissions(details.getId()));
    }
}
