package com.juvarya.order.dto;

import com.juvarya.order.dto.enums.DeliveryStatus;
import com.juvarya.order.dto.enums.OrderStatus;
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
    private AddressDTO userAddress;
    private Long businessId;
    private String businessName;
    private String  businessContactNumber;
    private Long shippingAddressId;

    private String notes;
    private String timmimgs;
    private AddressDTO businessAddress;
    private BigDecimal totalAmount;
    private BigDecimal totalTaxAmount;
    private Boolean taxInclusive;

    private String paymentStatus;
    private String paymentTransactionId;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String deliveryPartnerId;
    private String deliveryPartnerName;
    private String deliveryPartnerMobileNumber;

    private List<OrderItemDTO> orderItems;
}
