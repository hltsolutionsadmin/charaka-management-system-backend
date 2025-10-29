package com.juvarya.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {

    private Long id;
    private String name;
    private String shortCode;
    private boolean ignoreTax;
    private boolean discount;
    private String description;
    private Double price;
    private boolean available;
    private String productType;
    private String shopifyProductId;
    private String shopifyVariantId;
    private Long businessId;
    private String businessName;
    private Long categoryId;
    private String categoryName;

    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;
    private List<MediaDTO> media;


    private List<ProductAttributeDTO> attributes;
    private String status;
}
