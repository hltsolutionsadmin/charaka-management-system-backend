package com.juvarya.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDTO {

    private Long id;
    private Long userId;
    private String status;
    private String shopifyCartId;
    private ShippingAddressDTO shippingAddressDTO;
    private Long ShippingAddressId;
    private List<CartItemDTO> cartItems = new ArrayList<>();
    private Long paymentId;
    private Long businessId;
    private String businessName;
    private String notes;
    private Integer totalCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OfferDTO appliedOffer;
    private BigDecimal discountAmount;


}
