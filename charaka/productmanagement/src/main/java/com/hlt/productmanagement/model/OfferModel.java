package com.hlt.productmanagement.model;

import com.hlt.productmanagement.dto.enums.OfferTargetType;
import com.hlt.productmanagement.dto.enums.OfferType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "offers")
@Data
public class OfferModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false)
    private OfferType offerType;

    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "coupon_code", unique = true)
    private String couponCode;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "business_id", nullable = true)
    private Long businessId;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "offer_product_ids", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "product_id")
    private List<Long> productIds;

    @ElementCollection
    @CollectionTable(name = "offer_category_ids", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "category_id")
    private List<Long> categoryIds;

    @Column(name = "window_minutes")
    private Integer windowMinutes;

    @Column(name = "max_claims_per_window")
    private Integer maxClaimsPerWindow;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private OfferTargetType targetType;
}
