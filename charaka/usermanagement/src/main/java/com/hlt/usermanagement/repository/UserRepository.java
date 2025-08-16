package com.hlt.usermanagement.repository;

import com.hlt.usermanagement.model.RoleModel;
import com.hlt.usermanagement.model.UserModel;
import jakarta.validation.constraints.NotBlank;
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

    // Updated: Count users having a business in their businesses set with given businessId
    @Query("SELECT COUNT(u) FROM UserModel u JOIN u.businesses b WHERE b.id = :businessId")
    long countUsersByBusinessId(@Param("businessId") Long businessId);

    Optional<UserModel> findByPrimaryContactHash(String primaryContactHash);

    Optional<UserModel> findByEmailHash(String emailHash);

    Optional<UserModel> findByResetToken(@NotBlank String token);

    Optional<UserModel> findByUsernameOrEmail(String username, String email);
}
