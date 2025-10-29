package com.juvarya.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
}
