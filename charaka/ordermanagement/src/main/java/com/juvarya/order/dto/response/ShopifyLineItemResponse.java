package com.juvarya.order.dto.response;

import lombok.Data;

@Data
public class ShopifyLineItemResponse {
    private String lineItemId;
    private String variantId;
    private String variantTitle;
    private int quantity;


}