package com.juvarya.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationResultDTO {
    
    private boolean valid;
    private List<String> errors = new ArrayList<>();
    private BigDecimal cartTotal;
    private int itemCount;
    private boolean hasShippingAddress;
    

    public static CartValidationResultDTO valid(BigDecimal cartTotal, int itemCount, boolean hasShippingAddress) {
        CartValidationResultDTO result = new CartValidationResultDTO();
        result.setValid(true);
        result.setCartTotal(cartTotal);
        result.setItemCount(itemCount);
        result.setHasShippingAddress(hasShippingAddress);
        return result;
    }
    

    public static CartValidationResultDTO invalid(List<String> errors) {
        CartValidationResultDTO result = new CartValidationResultDTO();
        result.setValid(false);
        result.setErrors(errors);
        return result;
    }

    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }
}