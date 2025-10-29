package com.juvarya.order.dto.response;

import lombok.Data;

@Data
public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private String transactionId;
    private String status;
}
