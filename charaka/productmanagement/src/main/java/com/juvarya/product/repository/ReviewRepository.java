package com.juvarya.product.repository;

import com.juvarya.product.dto.enums.ReviewType;
import com.juvarya.product.model.ReviewModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewModel, Long> {

    Page<ReviewModel> findByProductIdAndType(Long productId, ReviewType type, Pageable pageable);
    Page<ReviewModel> findByBusinessIdAndType(Long businessId, ReviewType type, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM ReviewModel r WHERE r.productId = :productId AND r.type = 'PRODUCT'")
    Double averageRatingForProduct(@Param("productId") Long productId);

    @Query("SELECT AVG(r.rating) FROM ReviewModel r WHERE r.businessId = :businessId AND r.type = 'BUSINESS'")
    Double averageRatingForBusiness(@Param("businessId") Long businessId);

    Long countByProductIdAndType(Long productId, ReviewType type);
    Long countByBusinessIdAndType(Long businessId, ReviewType type);
}
