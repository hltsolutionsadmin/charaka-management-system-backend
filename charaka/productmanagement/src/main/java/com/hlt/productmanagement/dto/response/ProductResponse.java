package com.hlt.productmanagement.dto.response;

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

    public static <T> ProductResponse<T> success(T data, String message) {
        ProductResponse<T> response = new ProductResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        return response;
    }

    public static <T> ProductResponse<T> failure(T data, String message) {
        ProductResponse<T> response = new ProductResponse<>();
        response.setSuccess(false);
        response.setData(data);
        response.setMessage(message);
        return response;
    }
}
