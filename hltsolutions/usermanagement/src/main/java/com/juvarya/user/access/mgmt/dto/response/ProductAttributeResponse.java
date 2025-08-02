package com.juvarya.user.access.mgmt.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAttributeResponse {

    private Long id;
    private String attributeName;
    private String attributeValue;
}