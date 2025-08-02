package com.juvarya.user.access.mgmt.repository;


import com.juvarya.user.access.mgmt.dto.enums.PermissionType;
import com.juvarya.user.access.mgmt.model.UserPermissionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermissionModel, Long> {

    List<UserPermissionModel> findByUserId(Long userId);

    boolean existsByUserIdAndPermission(Long userId, PermissionType permission);
}
