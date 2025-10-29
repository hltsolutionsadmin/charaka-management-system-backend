package com.juvarya.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentTransactionDTO {



    private Long cartId;

    private BigDecimal amount;

    private String paymentId;

    private String razorpayOrderId;

    private String razorpaySignature;

    private BigDecimal planAmount;

    private String status;

    private Long ownerId;

    private Long customerId;

    private String refundId;

    private BigDecimal refundAmount;

    private LocalDateTime refundedAt;


}