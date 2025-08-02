package com.juvarya.user.access.mgmt.repository;


import com.juvarya.commonservice.enums.ERole;
import com.juvarya.commonservice.enums.UserVerificationStatus;
import com.juvarya.user.access.mgmt.model.RoleModel;
import com.juvarya.user.access.mgmt.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByUsername(String username);

    Boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) > 0 FROM UserModel u WHERE u.email = :email AND u.id <> :userId")
    Boolean existsByEmailAndNotUserId(@Param("email") String email, @Param("userId") Long userId);

    Optional<UserModel> findByEmail(String email);

    Page<UserModel> findByRoleModelsContaining(RoleModel roleModel, Pageable pageable);

    List<UserModel> findByRoleModelsContaining(RoleModel roleModel);

    Boolean existsByPrimaryContact(String primaryContact);

    Optional<UserModel> findByPrimaryContact(String primaryContact);

    Optional<UserModel> findByIdAndType(Long userId, String type);

    @Query("SELECT DISTINCT users FROM UserModel users JOIN users.addresses address WHERE address.postalCode IN :code AND users.type = :type")
    Page<UserModel> findByPostalCodesAndType(@Param("code") List<String> code, @Param("type") String type, Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserModel u WHERE u.b2bUnit.id = :businessId")
    long countUsersByBusinessId(@Param("businessId") Long businessId);


    @Query("SELECT u FROM UserModel u JOIN u.roleModels r " +
            "WHERE r.name = 'ROLE_USER' AND u.userVerificationStatus = :status " +
            "GROUP BY u.id HAVING COUNT(r) = 1")
    Page<UserModel> findAllUsersByRoleAndVerificationStatus(@Param("status") UserVerificationStatus status, Pageable pageable);


    @Query("SELECT u FROM UserModel u JOIN u.roleModels r " +
            "WHERE r.name = :role AND u.projectLoginFlags.deliveryPartner = false")
    List<UserModel> findByRoleAndDeliveryPartnerFlagFalse(@Param("role") ERole role);

}
