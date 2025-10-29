package com.juvarya.order.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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
