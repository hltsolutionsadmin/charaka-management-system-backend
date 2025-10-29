package com.juvarya.order.client;

import com.hlt.commonservice.dto.StandardResponse;
import com.juvarya.order.dto.OfferDTO;
import com.juvarya.order.dto.ProductDTO;
import com.juvarya.order.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "PRODUCTMGMT")
public interface ProductClient {

    @GetMapping("/api/product/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);

    @GetMapping("/api/product/api/offers/{offerId}")
    ResponseEntity<StandardResponse<OfferDTO>> getOfferById(@PathVariable("offerId") Long offerId);

    @GetMapping("/restaurant/{restaurantId}")
    ResponseEntity<ProductResponse<List<ProductDTO>>> getProductsByRestaurantId(@PathVariable("restaurantId") Long restaurantId);

    @PostMapping("/create")
    ResponseEntity<ProductResponse<ProductDTO>> createOrUpdateProduct(@RequestBody ProductDTO productDTO);

    @DeleteMapping("/{id}")
    ResponseEntity<ProductResponse<Void>> softDeleteProduct(@PathVariable("id") Long id);

    @PatchMapping("/{id}/toggle-availability")
    ResponseEntity<ProductResponse<ProductDTO>> toggleProductAvailability(@PathVariable("id") Long id);

    @GetMapping("/search")
    ResponseEntity<ProductResponse<List<ProductDTO>>> searchProducts(@RequestParam("query") String query);
}