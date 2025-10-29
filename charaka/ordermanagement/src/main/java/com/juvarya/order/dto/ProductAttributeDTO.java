package com.juvarya.order.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeDTO {
    private Long id;
    private String attributeName;
    private String attributeValue;
}
