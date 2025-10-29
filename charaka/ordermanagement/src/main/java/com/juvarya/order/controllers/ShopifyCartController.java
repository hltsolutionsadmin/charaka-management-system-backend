package com.juvarya.order.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.juvarya.order.dto.request.ShopifyOrderRequest;
import com.juvarya.order.dto.request.ShopifyCartAddRequest;
import com.juvarya.order.dto.response.ShopifyCartResponse;

import com.juvarya.order.service.ShopifyCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shopify/cart")
@RequiredArgsConstructor
public class ShopifyCartController {

    private final ShopifyCartService cartService;

    @PostMapping("/create")
    public ResponseEntity<String> createCart() {
        String response = cartService.createCart();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/addCart")
    public ResponseEntity<ShopifyCartResponse> addToCart(@RequestBody ShopifyCartAddRequest request) {
        try {
            ShopifyCartResponse response = cartService.addVariantsToCart(request.getCartId(), request.getVariantMap());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateCartId(@RequestParam String cartId) {
        try {
            boolean isValid = cartService.isCartIdValid(cartId);
            if (isValid) {
                return ResponseEntity.ok("Cart ID is valid.");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired Cart ID.");
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body("Error processing response: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }


    @PostMapping("/cart-details")
    public ResponseEntity<String> getCartDetails(@RequestBody Map<String, String> request) {
        String cartId = request.get("cartId");
        String response = cartService.getCartDetails(cartId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/variant/{variantId}")
    public ResponseEntity<String> checkVariant(@PathVariable Long variantId) {
        return cartService.getVariantById(variantId);
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<String> getCustomerId(@PathVariable String email) {
        return cartService.getCustomerId(email);
    }

    @PostMapping("/order")
    public ResponseEntity<String> placeShopifyOrder(@RequestBody ShopifyOrderRequest request) {
        return cartService.processOrderRequest(request);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchProductByName(@RequestParam String productName) {
        List<Map<String, Object>> products = cartService.searchProductByName(productName);
        return ResponseEntity.ok(products);
    }


}