package com.juvarya.product.controllers;

import com.juvarya.product.dto.BusinessWithProductsDTO;
import com.juvarya.product.dto.ProductDTO;
import com.juvarya.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/public/")
public class PublicProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }


    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> paginatedProducts = productService.getProductsByBusinessId(restaurantId, pageable, keyword);
        return ResponseEntity.ok(paginatedProducts);
    }


    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDTO>> getProductsByBuisiness(
            @RequestParam("businessId") Long businessId,
            @RequestParam("attributeValue") List<String> attributeValues,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {

        Page<ProductDTO> products = productService
                .getProductsByBusinessIdWithAttributeValue(businessId, attributeValues, keyword, pageable);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<ProductDTO> paginatedProducts = productService.searchProducts(query, pageable);
        return ResponseEntity.ok(paginatedProducts);
    }



    @GetMapping("/byCategoryNameAndBusiness")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategoryNameAndBusiness(
            @RequestParam String categoryName,
            @RequestParam Long businessId,
            Pageable pageable) {

        Page<ProductDTO> products = productService.getProductsByCategoryNameAndBusiness(categoryName, businessId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/nearby-search")
    public ResponseEntity<Page<BusinessWithProductsDTO>> searchNearbyProducts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") double radius,
            @RequestParam String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Fetch all nearby businesses with products (unpaged from DB, we'll paginate manually)
        Pageable unpaged = PageRequest.of(0, 1000);
        Page<BusinessWithProductsDTO> fullPage = productService.searchNearbyProducts(searchTerm, latitude, longitude, radius, categoryName, unpaged);

        // Optional filtering by search term (product name or business name)
        List<BusinessWithProductsDTO> filtered = fullPage.getContent().stream()
                .map(business -> {
                    List<ProductDTO> filteredProducts = business.getProducts().stream()
                            .filter(product -> {
                                if (searchTerm == null || searchTerm.isBlank()) return true;
                                String lower = searchTerm.toLowerCase();
                                return (product.getName() != null && product.getName().toLowerCase().contains(lower)) ||
                                        (product.getBusinessName() != null && product.getBusinessName().toLowerCase().contains(lower));
                            })
                            .toList();

                    if (filteredProducts.isEmpty()) return null;

                    BusinessWithProductsDTO dto = new BusinessWithProductsDTO();
                    dto.setId(business.getId());
                    dto.setName(business.getName());
                    dto.setCategory(business.getCategory());
                    dto.setApproved(business.isApproved());
                    dto.setCreatedAt(business.getCreatedAt());
                    dto.setAttributes(business.getAttributes());
                    dto.setProducts(filteredProducts);
                    return dto;
                })
                .filter(Objects::nonNull)
                .toList();

        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        Pageable pageable = PageRequest.of(page, size);

        if (start >= end) {
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Page<BusinessWithProductsDTO> pagedResult = new PageImpl<>(
                filtered.subList(start, end),
                pageable,
                filtered.size()
        );

        return ResponseEntity.ok(pagedResult);
    }


}
