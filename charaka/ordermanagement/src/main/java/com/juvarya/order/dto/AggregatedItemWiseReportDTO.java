package com.juvarya.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregatedItemWiseReportDTO {
    private String taxType;
    private String businessName;
    private String categoryName;
    private String productName;
    private BigDecimal total;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal avg;
}
