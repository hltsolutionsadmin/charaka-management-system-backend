package com.hlt.usermanagement.repository;

import com.hlt.usermanagement.model.AddressModel;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface B2BUnitRepository extends JpaRepository<B2BUnitModel, Long> {

    Optional<B2BUnitModel> findByUserModelAndBusinessNameIgnoreCase(UserModel userModel, String businessName);

    @Query(value = """
            SELECT * FROM (
                SELECT bu.*, 
                       (6371 * acos(
                           cos(radians(:latitude)) * cos(radians(bu.business_latitude)) *
                           cos(radians(bu.business_longitude) - radians(:longitude)) +
                           sin(radians(:latitude)) * sin(radians(bu.business_latitude))
                       )) AS distance
                FROM b2b_unit bu
                JOIN business_categories bc ON bu.category_id = bc.id
                WHERE bu.enabled = true
                  AND (:categoryName IS NULL OR LOWER(bc.name) = LOWER(:categoryName))
            ) AS calculated
            WHERE distance <= :radiusInKm
            ORDER BY distance ASC
            """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        SELECT bu.id,
                               (6371 * acos(
                                   cos(radians(:latitude)) * cos(radians(bu.business_latitude)) *
                                   cos(radians(bu.business_longitude) - radians(:longitude)) +
                                   sin(radians(:latitude)) * sin(radians(bu.business_latitude))
                               )) AS distance
                        FROM b2b_unit bu
                        JOIN business_categories bc ON bu.category_id = bc.id
                        WHERE bu.enabled = true
                          AND (:categoryName IS NULL OR LOWER(bc.name) = LOWER(:categoryName))
                    ) AS calculated
                    """,
            nativeQuery = true)
    Page<B2BUnitModel> findNearbyBusinessesWithCategoryFilter(@Param("latitude") double latitude,
                                                              @Param("longitude") double longitude,
                                                              @Param("radiusInKm") double radiusInKm,
                                                              @Param("categoryName") String categoryName,
                                                              Pageable pageable);

    @Query("""
            SELECT b 
            FROM B2BUnitModel b 
            JOIN AddressModel a ON a.user.id = b.userModel.id 
            WHERE a.postalCode = :postalCode
            """)
    Page<B2BUnitModel> findByUserAddressPostalCode(@Param("postalCode") String postalCode, Pageable pageable);

    @Query("SELECT b FROM B2BUnitModel b WHERE b.userModel.id = :userId")
    List<B2BUnitModel> findByUserModelId(@Param("userId") Long userId);

    Page<B2BUnitModel> findByCategory_NameOrderByCreationDateDesc(String categoryName, Pageable pageable);

    @Query("""
       SELECT b FROM B2BUnitModel b
       WHERE b.enabled = true
         AND LOWER(b.businessAddress.city) = LOWER(:city)
         AND LOWER(b.category.name) = LOWER(:categoryName)
   """)
    Page<B2BUnitModel> findByCityAndCategoryName(
            @Param("city") String city,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );

    Optional<B2BUnitModel> findByUserModel(UserModel userModel);

    @Query("SELECT b.businessAddress FROM B2BUnitModel b WHERE b.id = :unitId")
    Optional<AddressModel> findBusinessAddressByUnitId(@Param("unitId") Long unitId);
}
