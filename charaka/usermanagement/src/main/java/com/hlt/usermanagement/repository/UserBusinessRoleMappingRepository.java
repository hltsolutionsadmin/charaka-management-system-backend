package com.hlt.usermanagement.repository;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import com.hlt.usermanagement.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserBusinessRoleMappingRepository extends JpaRepository<UserBusinessRoleMappingModel, Long> {

    long countByUserIdAndRoleAndIsActiveTrue(Long userId, ERole role);

    boolean existsByUserIdAndB2bUnit_IdAndRoleAndIsActiveTrue(Long userId, Long hospitalId, ERole role);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM UserBusinessRoleMappingModel m " +
            "WHERE m.user.id = :userId AND m.b2bUnit.id = :b2bUnitId AND m.role = :role AND m.isActive = true")
    boolean existsByUserIdAndB2bUnitIdAndRoleAndIsActiveTrue(
            @Param("userId") Long userId,
            @Param("b2bUnitId") Long b2bUnitId,
            @Param("role") ERole role
    );

    @Query("""
                SELECT m.user.id
                FROM UserBusinessRoleMappingModel m
                WHERE m.role = :role AND m.isActive = true
                GROUP BY m.user.id
                HAVING COUNT(DISTINCT m.b2bUnit.id) < 2
            """)
    Page<Long> findUserIdsWithRoleMappedToLessThanTwoHospitals(@Param("role") ERole role, Pageable pageable);


    boolean existsByB2bUnitIdAndRole(Long businessId, ERole eRole);

    Page<UserBusinessRoleMappingModel> findByB2bUnitIdAndRole(Long hospitalId, ERole role, Pageable pageable);

    @Query("""
                SELECT ubr.user 
                FROM UserBusinessRoleMappingModel ubr
                WHERE ubr.role = :role
                  AND ubr.isActive = true
                  AND ubr.user.id NOT IN (
                      SELECT u.user.id
                      FROM UserBusinessRoleMappingModel u
                      WHERE u.b2bUnit.id = :hospitalId
                        AND u.role = :role
                        AND u.isActive = true
                  )
                GROUP BY ubr.user.id
                HAVING COUNT(ubr.b2bUnit.id) < 2
            """)
    Page<UserModel> findTelecallersAssignableToHospital(@Param("role") ERole role,
                                                        @Param("hospitalId") Long hospitalId,
                                                        Pageable pageable);



    List<UserBusinessRoleMappingModel> findByUserId(Long userId);

}
