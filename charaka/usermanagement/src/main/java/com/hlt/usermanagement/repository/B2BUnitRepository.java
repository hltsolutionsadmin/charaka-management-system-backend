package com.hlt.usermanagement.repository;

import com.hlt.usermanagement.model.AddressModel;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.UserModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface B2BUnitRepository extends JpaRepository<B2BUnitModel, Long> {


    Optional<B2BUnitModel> findByOwnerAndBusinessNameIgnoreCase(UserModel owner, String businessName);


    List<B2BUnitModel> findByOwnerId(@Param("userId") Long userId);

    Page<B2BUnitModel> findByCategory_NameOrderByCreationDateDesc(String categoryName, Pageable pageable);


    @Query("SELECT b FROM B2BUnitModel b WHERE b.businessAddress.postalCode = :postalCode")
    Page<B2BUnitModel> findByBusinessAddressPostalCode(@Param("postalCode") String postalCode, Pageable pageable);

    @Query("SELECT b.businessAddress FROM B2BUnitModel b WHERE b.id = :unitId")
    Optional<AddressModel> findBusinessAddressByUnitId(@Param("unitId") Long unitId);

    Optional<B2BUnitModel> findByOwner(UserModel owner);


}
