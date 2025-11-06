package com.hlt.productmanagement.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductTimingAttributesRequest {
	private List<ProductAttributeDTO> attributes;
	private String durationKeyword;
}
