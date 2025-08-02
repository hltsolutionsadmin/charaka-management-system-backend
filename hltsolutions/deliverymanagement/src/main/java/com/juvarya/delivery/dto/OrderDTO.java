package com.juvarya.delivery.dto;

import com.juvarya.delivery.dto.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {

    private Long id;
    private String orderNumber;
    private Long userId;
    private String username;
    private String mobileNumber;
    private AddressDTO address;
    private Long businessId;
    private String businessName;
    private Long shippingAddressId;

    private BigDecimal totalAmount;
    private BigDecimal totalTaxAmount;
    private Boolean taxInclusive;

    private String paymentStatus;
    private String paymentTransactionId;
    private OrderStatus orderStatus;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private List<OrderItemDTO> orderItems;
}
