package com.juvarya.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemWiseOrderReportDTO {

    // Business Info
    private Long businessId;
    private String businessName;

    // Product Info
    private Long productId;
    private String productName;
    private String categoryName;
    // Aggregated Sales Data
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal grossSales;
    private BigDecimal total;

    // Tax Info
    private Boolean taxable;
    private String taxType;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;

    // Analytics
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal avg;

    // Optional Order Metadata (used in non-aggregated reports)
    private String orderNumber;
    private LocalDateTime orderDate;
    private Long userId;
}
