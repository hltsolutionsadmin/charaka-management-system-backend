package com.juvarya.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.juvarya.product.dto.response.ProductAttributeResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class B2BUnitDTO {
    private Long id;

    private String businessName;

    private boolean approved;

    private String categoryName;

    private LocalDateTime creationDate;

    private UserDTO userDTO;

    private Set<ProductAttributeResponse> attributes;
}
