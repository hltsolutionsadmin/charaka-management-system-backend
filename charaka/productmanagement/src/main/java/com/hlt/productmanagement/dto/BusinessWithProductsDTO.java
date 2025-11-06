package com.hlt.productmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hlt.productmanagement.dto.response.ProductAttributeResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessWithProductsDTO {

    private Long id;
    private String name;
    private String category;
    private boolean isApproved;
    private LocalDateTime createdAt;

    private Set<ProductAttributeResponse> attributes;
    private List<ProductDTO> products;
}
