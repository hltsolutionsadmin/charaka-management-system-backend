package com.juvarya.order.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class PaymentTransactionResponse {
    private String message;
    private String status;
    private PaymentResponse data;
}
