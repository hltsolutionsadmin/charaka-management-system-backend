package com.juvarya.product.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juvarya.product.model.ProductModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Service
@Slf4j
public class ShopifyService {

    @Value("${shopify.access.token}")
    private String accessToken;

    @Value("${shopify.store.domain}")
    private String storeDomain;

    private static final String SHOPIFY_API_VERSION = "2024-01";

    public Map<String, String> createProductInShopify(ProductModel product) {
        String domain = storeDomain.trim();

        String url = String.format("https://%s/admin/api/%s/products.json", domain, SHOPIFY_API_VERSION);
        log.info("Shopify product creation URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Shopify-Access-Token", accessToken);

        Map<String, Object> payload = Map.of(
                "product", Map.of(
                        "title", product.getName(),
                        "body_html", product.getDescription(),
                        "vendor", "Juvi Product",
                        "product_type", product.getCategory().getName(),
                        "variants", List.of(Map.of(
                                "option1", "Default",
                                "price", product.getPrice().toString(),
                                "sku", product.getShortCode()
                        ))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info(" Shopify product successfully created: {}", product.getName());
                return extractProductAndVariantIds(response.getBody());
            } else {
                log.warn(" Unexpected response from Shopify: {}", response.getBody());
                throw new RuntimeException("Unexpected response from Shopify: " + response.getBody());
            }

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error(" Unauthorized: Invalid API Token - {}", e.getResponseBodyAsString());
            throw new RuntimeException("Unauthorized: Invalid Shopify access token");

        } catch (HttpClientErrorException e) {
            log.error(" Shopify API Error: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Shopify API Error: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("Internal Error during Shopify product creation: {}", e.getMessage(), e);
            throw new RuntimeException("Internal error occurred while creating product in Shopify", e);
        }
    }


    private Map<String, String> extractProductAndVariantIds(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode productNode = root.path("product");

            String productId = productNode.path("id").asText();
            String variantId = productNode.path("variants").get(0).path("id").asText();

            log.info("Extracted Product ID: {}", productId);
            log.info("Extracted Variant ID: {}", variantId);

            return Map.of("productId", productId, "variantId", variantId);
        } catch (Exception e) {
            log.error("Failed to extract IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Error extracting product and variant IDs from Shopify response");
        }
    }


}