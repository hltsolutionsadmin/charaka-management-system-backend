package com.juvarya.delivery.dto;


import com.juvarya.commonservice.dto.MediaDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private int quantity;
    private BigDecimal price;
    private Long entryNumber;
    private String productName;
    private List<MediaDTO> media;
    private BigDecimal taxAmount;
    private BigDecimal taxPercentage;
    private BigDecimal totalAmount;
    private boolean taxIgnored;

}
