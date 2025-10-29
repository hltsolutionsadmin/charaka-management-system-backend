package com.juvarya.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {

    private Long id;
    private String name;
    private String shortCode;
    private Boolean ignoreTax;
    private Boolean discount;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private String productType;
    private String shopifyProductId;
    private Long businessId;
    private String businessName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal taxPercentage;
    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;
    private List<MediaDTO> media;
    private List<ProductAttributeDTO> attributes;
}
