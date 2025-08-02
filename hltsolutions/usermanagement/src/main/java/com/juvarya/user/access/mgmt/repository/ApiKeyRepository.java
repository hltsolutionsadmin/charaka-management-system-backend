package com.juvarya.user.access.mgmt.repository;

import com.juvarya.user.access.mgmt.model.ApiKeyModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKeyModel, Long> {
    Optional<ApiKeyModel> findByHashedKeyAndActiveTrue(String hashedKey);

}
