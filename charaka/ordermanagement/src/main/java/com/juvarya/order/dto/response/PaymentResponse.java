package com.juvarya.order.dto.response;


import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private BigDecimal totalCreditAmount;

    private BigDecimal totalDebitAmount;

    private List<TransactionResponse> paymentTransactions;

}