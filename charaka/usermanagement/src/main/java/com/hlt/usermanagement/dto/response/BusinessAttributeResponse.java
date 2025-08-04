package com.hlt.usermanagement.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessAttributeResponse {

    private Long id;
    private String attributeName;
    private String attributeValue;
}