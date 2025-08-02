package com.juvarya.user.access.mgmt.repository;

import com.juvarya.user.access.mgmt.model.BusinessCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessCategoryRepository extends JpaRepository<BusinessCategoryModel, Long> {

    Optional<BusinessCategoryModel> findByName(String name);
    boolean existsByName(String name);
    List<BusinessCategoryModel> findByNameContainingIgnoreCase(String name);
    Optional<BusinessCategoryModel> findByNameIgnoreCase(String name);



}
