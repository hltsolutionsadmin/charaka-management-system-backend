package com.juvarya.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotBlank(message = "Payment transaction ID is required")
    private String paymentTransactionId;
    private Long previousOrderId;
    private List<OrderItemUpdateRequest> updates;
}
