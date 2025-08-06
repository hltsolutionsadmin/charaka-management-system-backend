package com.hlt.usermanagement.repository;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBusinessRoleMappingRepository extends JpaRepository<UserBusinessRoleMappingModel, Long> {

    long countByUserIdAndRoleAndIsActiveTrue(Long userId, ERole role);

    List<UserBusinessRoleMappingModel> findByB2bUnit_IdAndRole(Long hospitalId, ERole role);

    boolean existsByUserIdAndB2bUnit_IdAndRole(Long userId, Long hospitalId, ERole role);

    boolean existsByUserIdAndB2bUnit_IdAndRoleAndIsActiveTrue(Long userId, Long hospitalId, ERole role);

    boolean existsByUserIdAndB2bUnitAndRoleAndIsActiveTrue(Long userId, B2BUnitModel b2bUnit, ERole role);
}
