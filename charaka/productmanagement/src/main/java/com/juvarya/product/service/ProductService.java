package com.juvarya.product.service;

import com.juvarya.product.dto.BusinessWithProductsDTO;
import com.juvarya.product.dto.ProductAttributeDTO;
import com.juvarya.product.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    ProductDTO  createOrUpdateProduct(ProductDTO dto) throws IOException;

    ProductDTO getProductById(Long id);

    Page<ProductDTO> getProductsByBusinessId(Long businessId, Pageable pageable, String keyword);

    ProductDTO toggleProductAvailability(Long id);

    String deleteProductById(Long productId);

    Page<ProductDTO> searchProducts(String query, Pageable pageable);

    Page<ProductDTO> getProductsByBusinessIdWithAttributeValue(Long businessId, List<String> attrValues, String keyword, Pageable pageable);

    Page<ProductDTO> getProductsByCategoryNameAndBusiness(String categoryName, Long businessId, Pageable pageable);

    Page<BusinessWithProductsDTO> searchNearbyProducts(String productName, Double latitude, Double longitude, double radius, String categoryName, Pageable pageable);

    ProductDTO addProductTimingAttributes(Long id, List<ProductAttributeDTO> attributes, String durationKeyword);

    Page<ProductDTO> getProductsByBusinessIdAndAttributeValues(
            Long businessId,
            String attributeName,
            List<String> attributeValues,
            String searchTerm,
            Pageable pageable);
}



