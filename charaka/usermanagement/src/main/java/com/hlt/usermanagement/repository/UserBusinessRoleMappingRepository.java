package com.hlt.usermanagement.repository;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBusinessRoleMappingRepository extends JpaRepository<UserBusinessRoleMappingModel, Long> {

    long countByUserIdAndRoleAndIsActiveTrue(Long userId, ERole role);

    List<UserBusinessRoleMappingModel> findByB2bUnit_IdAndRole(Long hospitalId, ERole role);

    boolean existsByUserIdAndB2bUnit_IdAndRole(Long userId, Long hospitalId, ERole role);

    boolean existsByUserIdAndB2bUnit_IdAndRoleAndIsActiveTrue(Long userId, Long hospitalId, ERole role);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM UserBusinessRoleMappingModel m " +
            "WHERE m.user.id = :userId AND m.b2bUnit.id = :b2bUnitId AND m.role = :role AND m.isActive = true")
    boolean existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(
            @Param("userId") Long userId,
            @Param("b2bUnitId") Long b2bUnitId,
            @Param("role") ERole role
    );

    boolean existsByB2bUnitIdAndRole(Long businessId, ERole eRole);

}
