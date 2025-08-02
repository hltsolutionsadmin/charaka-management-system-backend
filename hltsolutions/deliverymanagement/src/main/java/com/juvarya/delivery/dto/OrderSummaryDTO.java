package com.juvarya.delivery.dto;

import com.juvarya.delivery.dto.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDTO {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private Long businessId;
    private String businessName;
    private Long shippingAddressId;
    private BigDecimal totalAmount;
    private OrderStatus status;
}
