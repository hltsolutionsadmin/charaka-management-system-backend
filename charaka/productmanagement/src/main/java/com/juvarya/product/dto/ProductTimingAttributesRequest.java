package com.juvarya.product.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductTimingAttributesRequest {
	private List<ProductAttributeDTO> attributes;
	private String durationKeyword;
}
