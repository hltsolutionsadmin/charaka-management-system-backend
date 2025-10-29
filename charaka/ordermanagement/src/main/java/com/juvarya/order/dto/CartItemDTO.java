package com.juvarya.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private Long variantId;
    private Long categoryId;
    private BigDecimal price;
    private List<MediaDTO> media;
    private Long cartId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
