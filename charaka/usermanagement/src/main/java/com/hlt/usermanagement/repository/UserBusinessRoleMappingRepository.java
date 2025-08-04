package com.hlt.usermanagement.repository;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBusinessRoleMappingRepository extends JpaRepository<UserBusinessRoleMappingModel, Long> {

    // Get all active mappings for a user
    List<UserBusinessRoleMappingModel> findByUserIdAndIsActiveTrue(Long userId);

    // Count active telecaller mappings
    long countByUserIdAndRoleAndIsActiveTrue(Long userId, ERole role);

    // Check if mapping already exists and is active
    boolean existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(Long userId, Long b2bUnitId, ERole role);
}
