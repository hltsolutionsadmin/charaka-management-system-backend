package com.hlt.usermanagement.repository;

import com.hlt.usermanagement.model.AddressModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<AddressModel, Long> {
    Page<AddressModel> findByUserId(Long userId, Pageable pageable);
    @Query("SELECT a FROM AddressModel a " +
            "JOIN a.user u " +
            "JOIN B2BUnitModel b ON b.owner.id = u.id " +
            "WHERE b.id = :businessId")
    Page<AddressModel> findAddressesByBusinessId(@Param("businessId") Long businessId, Pageable pageable);


    AddressModel findByUserIdAndIsDefaultTrue(Long userId);




}

