package com.juvarya.order.dto.request;

import lombok.Data;
import java.util.Map;

@Data
public class ShopifyCartAddRequest {
    private String cartId;
    private Map<String, Integer> variantMap;
}