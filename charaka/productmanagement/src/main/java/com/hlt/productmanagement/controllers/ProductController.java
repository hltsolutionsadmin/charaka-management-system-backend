package com.hlt.productmanagement.controllers;

import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.productmanagement.dto.BusinessWithProductsDTO;
import com.hlt.productmanagement.dto.ProductDTO;
import com.hlt.productmanagement.dto.ProductTimingAttributesRequest;
import com.hlt.productmanagement.dto.response.ProductResponse;
import com.hlt.productmanagement.service.ProductService;
import com.hlt.productmanagement.utils.UserBusinessAccessHelper;
import com.hlt.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/products")
@Slf4j
@AllArgsConstructor
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserBusinessAccessHelper userBusinessAccessHelper;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN')")
    public ResponseEntity<ProductResponse<ProductDTO>> createOrUpdateProduct(@ModelAttribute ProductDTO productDTO) {
        try {
            log.info("Creating/updating product: {}", productDTO);
            UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
            ProductDTO savedProduct = productService.createOrUpdateProduct(productDTO);
            log.info("Product created/updated with ID: {}", savedProduct.getId());
            return ResponseEntity.ok(ProductResponse.success(savedProduct));
        } catch (Exception e) {
            log.error("Error creating/updating product: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ProductResponse.error("Failed to create or update product: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/timings")
    public ResponseEntity<ProductResponse<ProductDTO>> addTimingsToProduct(
            @PathVariable Long id,
            @ModelAttribute ProductTimingAttributesRequest request) {

        log.info("Adding timings to product ID: {}, request: {}", id, request);
        ProductDTO updatedProduct = productService.addProductTimingAttributes(id, request.getAttributes(), request.getDurationKeyword());
        return ResponseEntity.ok(ProductResponse.success(updatedProduct, "Timings added successfully as attributes"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        try {
            log.info("Fetching product by ID: {}", id);
            ProductDTO product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (HltCustomerException ex) {
            log.warn("Product not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            log.error("Error fetching product by ID: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<ProductResponse<Page<ProductDTO>>>getProductsByBussinessId(
            @PathVariable Long businessId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching products for business ID: {}, keyword: {}", businessId, keyword);
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDTO> paginatedProducts = productService.getProductsByBusinessId(businessId, pageable, keyword);
            return ResponseEntity.ok(ProductResponse.success(paginatedProducts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ProductResponse.error("Failed to fetch products for business: " + e.getMessage()));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDTO>> getProductsByBusinessIdWithAttributeValue(
            @RequestParam("businessId") Long businessId,
            @RequestParam(value = "attributeValue", required = false) List<String> attributeValues,
            @RequestParam(value = "keyword", required = false) String keyword,
            Pageable pageable) {

        log.info("Filtering products for business ID: {}, attributes: {}, keyword: {}", businessId, attributeValues, keyword);
        Page<ProductDTO> products = productService.getProductsByBusinessIdWithAttributeValue(businessId, attributeValues, keyword, pageable);
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<ProductResponse<ProductDTO>> toggleProductAvailability(@PathVariable Long id) {
        try {
            log.info("Toggling availability for product ID: {}", id);
            ProductDTO updatedProduct = productService.toggleProductAvailability(id);
            return ResponseEntity.ok(ProductResponse.success(updatedProduct));
        } catch (Exception e) {
            log.error("Failed to toggle product availability: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ProductResponse.error("Failed to toggle product availability: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ProductResponse<Page<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy) {

        try {
            log.info("Searching products with query: {}", query);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            Page<ProductDTO> paginatedProducts = productService.searchProducts(query, pageable);
            return ResponseEntity.ok(ProductResponse.success(paginatedProducts));
        } catch (Exception e) {
            log.error("Failed to search products: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ProductResponse.error("Failed to search products: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ProductResponse<String>> deleteProduct(@PathVariable Long productId) {
        try {
            log.info("Deleting product ID: {}", productId);
            String message = productService.deleteProductById(productId);
            return ResponseEntity.ok(ProductResponse.success(message));
        } catch (HltCustomerException e) {
            log.warn("Product not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ProductResponse.error("Product not found: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to delete product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ProductResponse.error("Failed to delete product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/bulk")
    public ResponseEntity<ProductResponse<String>> deleteProducts(@RequestBody List<Long> productIds) {
        log.info("Deleting products: {}", productIds);
        String message = productService.deleteProductsByIds(productIds);
        return ResponseEntity.ok(ProductResponse.success(message));
    }


    @GetMapping("/byCategoryNameAndBusiness")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategoryNameAndBusiness(
            @RequestParam String categoryName,
            @RequestParam Long businessId,
            Pageable pageable) {

        log.info("Fetching products by category: {} and businessId: {}", categoryName, businessId);
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

        log.info("Searching nearby products with searchTerm: {}, location: ({}, {}), radius: {}, category: {}", searchTerm, latitude, longitude, radius, categoryName);
        Pageable unpaged = PageRequest.of(0, 1000);
        Page<BusinessWithProductsDTO> fullPage = productService.searchNearbyProducts(searchTerm, latitude, longitude, radius, categoryName, unpaged);

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

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        Pageable pageable = PageRequest.of(page, size);

        if (start >= end) {
            log.info("No nearby products found after filtering.");
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Page<BusinessWithProductsDTO> pagedResult = new PageImpl<>(
                filtered.subList(start, end), pageable, filtered.size());

        log.info("Returning {} nearby businesses with products", pagedResult.getNumberOfElements());
        return ResponseEntity.ok(pagedResult);
    }

        @GetMapping("/search-by-attribute")
        public ResponseEntity<StandardResponse<Page<ProductDTO>>> getProductsByAttribute(
                @RequestParam Long businessId,
                @RequestParam String attributeName,
                @RequestParam List<String> attributeValues,
                @RequestParam(defaultValue = "") String search,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {

            Pageable pageable = PageRequest.of(page, size);

            Page<ProductDTO> result = productService.getProductsByBusinessIdAndAttributeValues(
                    businessId, attributeName, attributeValues, search, pageable
            );

            return ResponseEntity.ok(StandardResponse.page("Products fetched successfully",result));
        }
    @PostMapping("/bulk-upload")
    @PreAuthorize("hasAnyRole('ROLE_USER_ADMIN')")
    public ResponseEntity<StandardResponse<List<ProductDTO>>> bulkUploadProducts(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Bulk uploading products from file: {}", file.getOriginalFilename());
            List<ProductDTO> uploadedProducts = productService.bulkUploadProducts(file);
            log.info("Bulk upload completed. Total products uploaded: {}", uploadedProducts.size());

            return ResponseEntity.ok(StandardResponse.list("Products uploaded successfully", uploadedProducts));

        } catch (HltCustomerException e) {
            log.error("Bulk upload failed: {}", e.getErrorCode().getMessage(), e);

            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(StandardResponse.error(e.getErrorCode().getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during bulk upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(StandardResponse.error("Failed to upload products"));
        }
    }


}