package com.juvarya.order.dao;


import com.juvarya.order.dto.enums.DeliveryStatus;
import com.juvarya.order.dto.enums.OrderStatus;
import com.juvarya.order.entity.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, Long>, JpaSpecificationExecutor<OrderModel> {

    List<OrderModel> findByUserId(Long userId);

    Page<OrderModel> findByUserId(Long userId, Pageable pageable);

    OrderModel findByIdAndUserId(Long id, Long userId);

    Page<OrderModel> findByBusinessId(Long businessId, Pageable pageable);

    Optional<OrderModel> findByOrderNumber(String orderNumber);


    List<OrderModel> findByCreatedDateBetweenAndBusinessId(LocalDateTime start, LocalDateTime end, Long businessId);

    @Query(value = """
                SELECT
                    CASE WHEN p.is_taxable = true THEN 'Taxable' ELSE 'Non-Taxable' END AS taxType,
                    o.business_name,
                    p.category_name,
                    p.name,
                    SUM(oi.total_amount),
                    MIN(oi.total_amount),
                    MAX(oi.total_amount),
                    AVG(oi.total_amount)
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE o.created_date BETWEEN :from AND :to
                  AND o.business_id = :businessId
                GROUP BY taxType, o.business_name, p.category_name, p.name
            """, nativeQuery = true)
    List<Object[]> fetchItemWiseAggregatedDataNative(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("businessId") Long businessId);


    @Query("""
                SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
                FROM OrderModel o
                JOIN o.orderItems item
                WHERE o.userId = :userId
                  AND item.productId = :productId
                  AND o.businessId = :businessId
                  AND o.orderStatus IN :statuses
            """)
    boolean existsProductOrder(@Param("userId") Long userId, @Param("productId") Long productId, @Param("businessId") Long businessId, @Param("statuses") List<OrderStatus> statuses);

    // 2. Check if a user ordered from a business
    @Query("""
                SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
                FROM OrderModel o
                WHERE o.userId = :userId
                  AND o.businessId = :businessId
                  AND o.orderStatus IN :statuses
            """)
    boolean existsBusinessOrder(@Param("userId") Long userId, @Param("businessId") Long businessId, @Param("statuses") List<OrderStatus> statuses);

    Page<OrderModel> findByOrderStatus(OrderStatus status, Pageable pageable);

    Page<OrderModel> findByBusinessIdAndOrderStatus(Long businessId, OrderStatus status, Pageable pageable);


    Page<OrderModel> findByDeliveryPartnerId(String partnerId, Pageable pageable);

    Page<OrderModel> findByDeliveryPartnerIdAndDeliveryStatus(String deliveryPartnerId, DeliveryStatus status, Pageable pageable);

    @Query("SELECT o FROM OrderModel o WHERE o.deliveryPartnerId = :deliveryPartnerId " +
            "AND o.deliveryStatus = :deliveryStatus " +
            "AND o.createdDate BETWEEN :startOfDay AND :endOfDay")
    Page<OrderModel> findByPartnerAndStatusAndToday(
            @Param("deliveryPartnerId") String deliveryPartnerId,
            @Param("deliveryStatus") DeliveryStatus deliveryStatus,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            Pageable pageable);


    Page<OrderModel> findByDeliveryStatus(DeliveryStatus status, Pageable pageable);

    boolean existsByDeliveryPartnerIdAndDeliveryStatus(String deliveryPartnerId, DeliveryStatus deliveryStatus);

}
