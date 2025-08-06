package com.hlt.usermanagement.repository;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBusinessRoleMappingRepository extends JpaRepository<UserBusinessRoleMappingModel, Long> {

    long countByUserIdAndRoleAndIsActiveTrue(Long userId, ERole role);

    List<UserBusinessRoleMappingModel> findByB2BUnitIdAndRole(Long hospitalId, ERole role);

    boolean existsByUserIdAndB2BUnitIdAndRole(Long userId, Long hospitalId, ERole role);

    boolean existsByUserIdAndB2BUnitIdAndRoleAndIsActiveTrue(Long telecallerId, Long hospitalId, ERole eRole);
}
