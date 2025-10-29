package com.juvarya.order.dto;

import com.juvarya.order.dto.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryDTO {
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String businessName;
    private Long businessId;
    private Long shippingAddressId;
    private BigDecimal totalAmount;
    private OrderStatus status;
}
