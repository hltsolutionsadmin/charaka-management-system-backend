package com.juvarya.order.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "entry_number")
    private Long entryNumber;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "tax_percentage")
    private BigDecimal taxPercentage;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "is_tax_ignored", nullable = false)
    private Boolean taxIgnored;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @ToString.Exclude
    @JsonBackReference
    private OrderModel order;
}
