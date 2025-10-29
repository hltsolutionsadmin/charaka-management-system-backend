package com.juvarya.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.juvarya.order.dto.request.ShopifyOrderRequest;
import com.juvarya.order.dto.response.ShopifyCartResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface ShopifyCartService {
    String createCart();

    ShopifyCartResponse addVariantsToCart(String cartId, Map<String, Integer> variantMap) throws JsonProcessingException;

    String getCartDetails(String cartId);

    ResponseEntity<String> getVariantById(Long variantId);

    ResponseEntity<String> getCustomerId(String email);

    ResponseEntity<String> processOrderRequest(ShopifyOrderRequest request);

    boolean isCartIdValid(String cartId) throws JsonProcessingException;

    List<Map<String, Object>> searchProductByName(String productName);


}

