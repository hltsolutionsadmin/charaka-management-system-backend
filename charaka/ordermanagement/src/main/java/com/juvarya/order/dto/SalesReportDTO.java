package com.juvarya.order.dto;

import com.juvarya.order.dto.enums.OrderStatus;
import com.juvarya.order.dto.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SalesReportDTO {
    private String orderNumber;
    private LocalDateTime orderTime;
    private String serverName;
    private String orderType;
    private OrderStatus status;
    private PaymentType paymentType;
    private Double totalAmount;
    private Double discountPercentage;
    private Double ignoreTax;
}
