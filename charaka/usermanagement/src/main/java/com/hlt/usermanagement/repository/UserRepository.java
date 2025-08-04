package com.hlt.usermanagement.repository;


import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.enums.UserVerificationStatus;
import com.hlt.usermanagement.model.RoleModel;
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



    @Query("SELECT COUNT(u) FROM UserModel u WHERE u.b2bUnit.id = :businessId")
    long countUsersByBusinessId(@Param("businessId") Long businessId);

    Optional<UserModel> findByPrimaryContactHash(String primaryContact);

    Optional<UserModel> findByEmailHash(String emailHash);
}
