package com.juvarya.order.entity;

import com.juvarya.order.dto.enums.DeliveryStatus;
import com.juvarya.order.dto.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class OrderModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_tax_amount")
    private BigDecimal totalTaxAmount;

    @Column(name = "tax_inclusive")
    private Boolean taxInclusive;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "delivery_partner_id")
    private String deliveryPartnerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemModel> orderItems;

    @Column(name = "notes")
    private String notes;

    @Column(name = "timmimgs")
    private String timmimgs;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
