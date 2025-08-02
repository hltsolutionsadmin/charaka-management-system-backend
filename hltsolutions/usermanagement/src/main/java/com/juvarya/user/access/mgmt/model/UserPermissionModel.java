package com.juvarya.user.access.mgmt.model;

import com.juvarya.user.access.mgmt.dto.enums.PermissionType;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "user_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserPermissionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PermissionType permission;


    public UserPermissionModel(Long userId, PermissionType permission) {
        this.userId = userId;
        this.permission = permission;
    }
}

