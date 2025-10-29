package com.juvarya.order.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.juvarya.order.dto.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDTO {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private Long businessId;
    private Long shippingAddressId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
}
