package com.juvarya.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private String status;
    private T data;

    public static <T> ApiResponse<T> single(String message, T data) {
        return new ApiResponse<>(message, "success", data);
    }

    public static <T> ApiResponse<T> list(String message, T dataList) {
        return new ApiResponse<>(message, "success", dataList);
    }

    public static <T> ApiResponse<Page<T>> page(String message, Page<T> data) {
        return new ApiResponse<>(message, "success", data);
    }

    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(message, "success", null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, "error", null);
    }
}
