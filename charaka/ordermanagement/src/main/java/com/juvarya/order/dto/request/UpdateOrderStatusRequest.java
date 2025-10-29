package com.juvarya.order.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank
    private String orderNumber;

    @NotBlank
    private String orderStatus;
}
