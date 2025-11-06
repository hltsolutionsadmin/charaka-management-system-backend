package com.hlt.productmanagement.repository;

import com.hlt.productmanagement.model.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {

    boolean existsByBusinessIdAndShortCode(Long businessId, String shortCode);

    Page<ProductModel> findByBusinessIdIn(List<Long> businessIds, Pageable pageable);

    // Search by keyword in name, description or shortcode
    @Query("""
        SELECT p FROM ProductModel p
        WHERE p.businessId = :businessId AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(p.shortCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<ProductModel> searchByKeywordAndBusinessId(
            @Param("businessId") Long businessId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT p FROM ProductModel p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :nameKeyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :descriptionKeyword, '%')) OR " +
            "LOWER(p.shortCode) LIKE LOWER(CONCAT('%', :shortCodeKeyword, '%'))")
    Page<ProductModel> searchProducts(
            @Param("nameKeyword") String nameKeyword,
            @Param("descriptionKeyword") String descriptionKeyword,
            @Param("shortCodeKeyword") String shortCodeKeyword,
            Pageable pageable
    );

    // Search by Category Name
    @Query("""
        SELECT p FROM ProductModel p
        WHERE LOWER(p.category.name) = LOWER(:categoryName)
        AND p.businessId = :businessId
    """)
    Page<ProductModel> findByCategoryNameAndBusinessId(
            @Param("categoryName") String categoryName,
            @Param("businessId") Long businessId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT p
    FROM ProductModel p
    JOIN p.attributes a
    WHERE p.businessId = :businessId
      AND a.attributeValue IN :attributeValues
      AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                         OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<ProductModel> findByBusinessIdAndAttributeValueIn(
            @Param("businessId") Long businessId,
            @Param("attributeValues") List<String> attributeValues,
            @Param("keyword") String keyword,
            Pageable pageable);


    Page<ProductModel> findByBusinessId(Long businessId, Pageable pageable);


    @Query("SELECT p FROM ProductModel p " +
            "JOIN p.attributes a " +
            "WHERE p.businessId = :businessId " +
            "AND a.attributeName = :attributeName " +
            "AND a.attributeValue IN :attributeValues " +
            "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<ProductModel> findByBusinessIdAndAttributeValuesWithSearch(
            @Param("businessId") Long businessId,
            @Param("attributeName") String attributeName,
            @Param("attributeValues") List<String> attributeValues,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);


    Page<ProductModel> findByBusinessIdInAndNameContainingIgnoreCase(List<Long> businessIds, String name, Pageable pageable);


}