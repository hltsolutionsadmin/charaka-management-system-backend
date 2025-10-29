package com.juvarya.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private Long transactionId;
    private LocalDateTime transactionDate;
    private String status;
}