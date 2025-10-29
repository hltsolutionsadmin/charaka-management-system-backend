package com.juvarya.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopifyCartResponse {
    private String cartId;
    private String checkoutUrl;
    private List<ShopifyLineItemResponse> lineItems;
}