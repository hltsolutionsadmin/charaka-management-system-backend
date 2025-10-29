package com.juvarya.order.dto.request;

import com.juvarya.order.dto.CartItemDTO;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class CartRequest {

    private String notes;

    @Valid
    private List<CartItemDTO> items;
}
