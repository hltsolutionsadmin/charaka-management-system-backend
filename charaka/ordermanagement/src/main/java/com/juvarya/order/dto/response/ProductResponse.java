package com.juvarya.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ProductResponse<T> success(T data) {
        return new ProductResponse<>(true, "Request was successful", data);
    }

    public static <T> ProductResponse<T> error(String message) {
        return new ProductResponse<>(false, message, null);
    }
}
