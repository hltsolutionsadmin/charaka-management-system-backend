package com.hlt.productmanagement.dto;

import com.hlt.productmanagement.dto.enums.OfferTargetType;
import com.hlt.productmanagement.dto.enums.OfferType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OfferDTO {

    private Long id;

    private String name;

    private OfferType offerType;

    private BigDecimal value;

    private BigDecimal minOrderValue;

    private String couponCode;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long businessId;

    private Boolean active;

    private String description;

    private List<Long> productIds;

    private List<Long> categoryIds;

    private OfferTargetType targetType;
}
