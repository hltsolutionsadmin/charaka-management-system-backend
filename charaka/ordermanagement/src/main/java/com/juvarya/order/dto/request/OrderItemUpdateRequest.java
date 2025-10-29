package com.juvarya.order.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemUpdateRequest {
    private Long productId;
    private Integer quantity;
}
