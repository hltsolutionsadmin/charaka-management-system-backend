package com.juvarya.product.repository;

import com.juvarya.product.model.OfferModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<OfferModel, Long> {
    @Query("SELECT o FROM OfferModel o WHERE o.businessId = :businessId " +
            "AND (:active IS NULL OR o.active = :active)")
    Page<OfferModel> findOffers(Long businessId, Boolean active, Pageable pageable);


    List<OfferModel> findByBusinessId(Long businessId);

    Page<OfferModel> findByBusinessIdAndActive(Long businessId, Boolean active, Pageable pageable);

    @Query("SELECT o FROM OfferModel o " +
            "WHERE o.businessId = :businessId " +
            "AND (:active IS NULL OR o.active = :active) " +
            "AND (" +
            "   :keyword IS NULL OR :keyword = '' OR " +
            "   LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            ")")
    Page<OfferModel> searchOffers(
            @Param("businessId") Long businessId,
            @Param("active") Boolean active,
            @Param("keyword") String keyword,
            Pageable pageable);



    List<OfferModel> findByActiveTrueAndEndDateBefore(LocalDateTime now);

    Optional<OfferModel> findByCouponCode(String couponCode);

}