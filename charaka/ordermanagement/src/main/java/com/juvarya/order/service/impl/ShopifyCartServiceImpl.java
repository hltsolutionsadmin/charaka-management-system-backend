package com.juvarya.order.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.juvarya.order.client.ProductClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.juvarya.order.dto.request.ShopifyOrderRequest;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.dto.response.ShopifyCartResponse;
import com.juvarya.order.service.ShopifyCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopifyCartServiceImpl implements ShopifyCartService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${shoify.acces.storeFront}")
    private String storeToken;

    @Value("${shopify.access.token}")
    private String accessToken;

    @Value("${shopify.store.domain}")
    private String shopDomain;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductClient productClient;

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Shopify-Storefront-Access-Token", storeToken);
        return headers;
    }

    private String shopifyEndpoint() {
        return "https://" + shopDomain.trim() + "/api/2023-10/graphql.json";
    }

    @Override
    public String createCart() {
        String query = """
                {
                  "query": "mutation { cartCreate { cart { id checkoutUrl } userErrors { message } } }"
                }
                """;

        HttpEntity<String> entity = new HttpEntity<>(query, buildHeaders());
        try {
            ResponseEntity<String> response = restTemplate.exchange(shopifyEndpoint(), HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully created cart: {}", response.getBody());
                return extractCartIdFromResponse(response.getBody());
            } else {
                log.error("Failed to create cart. Response: {}", response.getBody());
                throw new RuntimeException("Failed to create cart, status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error during cart creation request", e);
            throw new RuntimeException("Error during cart creation request", e);
        }
    }

    private String extractCartIdFromResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode cartNode = rootNode.path("data").path("cartCreate").path("cart");

            String cartId = cartNode.path("id").asText();
            log.info("Extracted Cart ID: {}", cartId);

            return cartId;
        } catch (Exception e) {
            log.error("Error extracting Cart ID from response: {}", e.getMessage(), e);
            throw new RuntimeException("Error extracting Cart ID from response", e);
        }
    }


    public String getVariantId(String productId) {
        String url = String.format("https://%s/admin/api/2024-01/products/%s.json", shopDomain, productId);
        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            JsonNode json = new ObjectMapper().readTree(response.getBody());
            return json.path("product").path("variants").get(0).path("id").asText(); // get first variant
        } catch (Exception e) {
            log.error("Error fetching variant ID: {}", e.getMessage());
            throw new RuntimeException("Unable to extract variant ID.");
        }
    }
    @Override
    public ShopifyCartResponse addVariantsToCart(String cartId, Map<String, Integer> variantMap) throws JsonProcessingException {
        String linesInput = variantMap.entrySet().stream()
                .map(e -> String.format("""
                {
                  merchandiseId: "%s",
                  quantity: %d
                }
            """, e.getKey(), e.getValue()))
                .collect(Collectors.joining(","));

        // Step 2: Build GraphQL mutation string
        String mutation = String.format("""
    mutation {
      cartLinesAdd(cartId: "%s", lines: [%s]) {
        cart {
          id
          checkoutUrl
          lines(first: 5) {
            edges {
              node {
                id
                quantity
                merchandise {
                  ... on ProductVariant {
                    id
                    title
                    price {
                      amount
                      currencyCode
                    }
                  }
                }
              }
            }
          }
        }
        userErrors {
          field
          message
        }
      }
    }
    """, cartId, linesInput);

        // Step 3: Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(storeToken);
        headers.set("X-Shopify-Storefront-Access-Token", storeToken); // optional: use if required for Storefront access
        headers.set("Accept", "application/json");

        // Step 4: Build the request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", mutation);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        // Step 5: Call Shopify Storefront API
        String url = "https://" + shopDomain + "/api/2024-01/graphql.json";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Step 6: Parse response
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode cartNode = root.path("data").path("cartLinesAdd").path("cart");

        ShopifyCartResponse cartResponse = new ShopifyCartResponse();
        cartResponse.setCartId(cartNode.path("id").asText());
        cartResponse.setCheckoutUrl(cartNode.path("checkoutUrl").asText());

        return cartResponse;
    }


    @Override
    public String getCartDetails(String cartId) {
        String query = String.format("""
        {
          "query": "query { cart(id: \\\"%s\\\") { id checkoutUrl lines(first: 10) { edges { node { id quantity merchandise { ... on ProductVariant { id title price product { id } } } } } } } }"
        }
        """, cartId);

        HttpEntity<String> entity = new HttpEntity<>(query, buildHeaders());
        ResponseEntity<String> response = restTemplate.exchange(shopifyEndpoint(), HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Fetched cart details: {}", response.getBody());
            try {
                // Parse the response
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode cartNode = root.path("data").path("cart");

                // Create a new object node to hold the modified response
                ObjectNode modifiedRoot = objectMapper.createObjectNode();
                ObjectNode modifiedData = modifiedRoot.putObject("data");
                modifiedData.set("cart", cartNode);

                // Add businessName to each line item
                JsonNode linesNode = cartNode.path("lines");
                if (!linesNode.isMissingNode() && !linesNode.isNull()) {
                    JsonNode edgesNode = linesNode.path("edges");
                    if (edgesNode.isArray()) {
                        for (JsonNode edgeNode : edgesNode) {
                            JsonNode nodeNode = edgeNode.path("node");
                            JsonNode merchandiseNode = nodeNode.path("merchandise");
                            JsonNode productNode = merchandiseNode.path("product");

                            if (!productNode.isMissingNode() && !productNode.isNull()) {
                                String productId = productNode.path("id").asText();
                                try {
                                    // Extract the numeric ID from the Shopify ID (format: gid://shopify/Product/1234567890)
                                    String numericId = null;
                                    if (productId != null && productId.contains("/")) {
                                        numericId = productId.substring(productId.lastIndexOf("/") + 1);
                                    }

                                    if (numericId != null && !numericId.isEmpty()) {
                                        try {
                                            // Get product information from ProductClient
                                            ProductDTO product = productClient.getProductById(Long.parseLong(numericId));
                                            if (product != null && product.getBusinessName() != null) {
                                                // Add businessName to the merchandise node
                                                ((ObjectNode) merchandiseNode).put("businessName", product.getBusinessName());
                                            }
                                        } catch (NumberFormatException nfe) {
                                            log.error("Error parsing product ID '{}': {}", numericId, nfe.getMessage());
                                        } catch (Exception e) {
                                            log.error("Error retrieving product information for ID '{}': {}", numericId, e.getMessage());
                                        }
                                    } else {
                                        log.warn("Could not extract numeric ID from product ID: {}", productId);
                                    }
                                } catch (Exception e) {
                                    log.error("Error processing product ID '{}': {}", productId, e.getMessage());
                                }
                            }
                        }
                    }
                }

                // Return the modified response
                return objectMapper.writeValueAsString(modifiedRoot);
            } catch (Exception e) {
                log.error("Error processing cart details: {}", e.getMessage());
                return response.getBody();
            }
        } else {
            log.error("Failed to fetch cart details: {}", response.getBody());
            throw new RuntimeException("Failed to fetch cart details");
        }
    }


    @Override
    public ResponseEntity<String> getVariantById(Long variantId) {
        if (!shopDomain.startsWith("https://")) {
            shopDomain = "https://" + shopDomain;
        }

        String url = shopDomain + "/admin/api/2023-10/variants/" + variantId + ".json";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Shopify-Access-Token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
        }
    }


    @Override
    public ResponseEntity<String> getCustomerId(String email) {
        if (!shopDomain.startsWith("https://")) {
            shopDomain = "https://" + shopDomain;
        }
        String url = shopDomain + "/admin/api/2023-10/customers.json?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Shopify-Access-Token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
        }
    }


    @Override
    public boolean isCartIdValid(String cartId) throws JsonProcessingException {
        String query = """
            query getCart($cartId: ID!) {
              cart(id: $cartId) {
                id
                checkoutUrl
              }
            }
        """;

        Map<String, Object> variables = Map.of("cartId", cartId);
        Map<String, Object> payload = Map.of(
                "query", query,
                "variables", variables
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Shopify-Storefront-Access-Token", accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        if (!shopDomain.startsWith("https://")) {
            shopDomain = "https://" + shopDomain;
        }
        String url = shopDomain + "/api/2023-10/graphql.json";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode cartNode = root.path("data").path("cart");

        if (cartNode.isMissingNode() || cartNode.isNull()) {
            return false;
        }

        JsonNode errors = root.path("errors");
        if (errors.isArray() && errors.size() > 0) {
            return false;
        }

        return true;
    }

    @Override
    public ResponseEntity<String> processOrderRequest(ShopifyOrderRequest request) {
        String formattedDomain = shopDomain.startsWith("https://") ? shopDomain : "https://" + shopDomain;
        String url = formattedDomain + "/admin/api/2023-10/orders.json";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Shopify-Access-Token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode orderNode = root.putObject("order");

            ObjectNode customerNode = orderNode.putObject("customer");
            customerNode.put("first_name", request.getOrder().getCustomer().getFirst_name());
            customerNode.put("last_name", request.getOrder().getCustomer().getLast_name());
            customerNode.put("email", request.getOrder().getCustomer().getEmail());

            ArrayNode lineItemsNode = orderNode.putArray("line_items");
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ShopifyOrderRequest.Order.LineItem item : request.getOrder().getLine_items()) {
                ObjectNode itemNode = lineItemsNode.addObject();
                itemNode.put("variant_id", item.getVariant_id());
                itemNode.put("quantity", item.getQuantity());
                if (item.getPrice() != null) {
                    totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }

            orderNode.put("financial_status", "pending");

            ArrayNode transactionsNode = orderNode.putArray("transactions");
            ObjectNode transaction = transactionsNode.addObject();
            transaction.put("kind", "sale");
            transaction.put("status", "pending");
            transaction.put("gateway", "manual");
            transaction.put("amount", totalAmount.toPlainString());

            orderNode.set("shipping_address", objectMapper.valueToTree(request.getOrder().getShipping_address()));

            orderNode.put("send_receipt", true);
            orderNode.put("send_fulfillment_receipt", false);

            String payload = objectMapper.writeValueAsString(root);
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error serializing order payload: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error placing Shopify order: " + ex.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> searchProductByName(String productName) {
        String trimmedDomain = shopDomain.trim();

        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(trimmedDomain)
                .path("/admin/api/2024-01/products.json")
                .queryParam("title", productName)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Shopify-Access-Token", accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        log.info("Sending GET request to Shopify: {}", url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode productsNode = root.get("products");

                List<Map<String, Object>> result = new ArrayList<>();
                if (productsNode != null && productsNode.isArray()) {
                    for (JsonNode product : productsNode) {
                        result.add(objectMapper.convertValue(product, Map.class));
                    }
                }

                log.info("🛒 Fetched {} product(s) from Shopify for '{}'", result.size(), productName);
                return result;
            } else {
                log.error("Shopify responded with status: {} | body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Shopify response error while searching product.");
            }
        } catch (Exception e) {
            log.error("Internal error during Shopify product search", e);
            throw new RuntimeException("Internal error during Shopify product search", e);
        }
    }

}