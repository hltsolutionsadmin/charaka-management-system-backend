package com.juvarya.user.access.mgmt.dto.response;

public class RestaurantResponse<T> {
    private int status;
    private String message;
    private T data;
    public RestaurantResponse() {
    }

    public RestaurantResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> RestaurantResponse<T> success(T data) {
        return new RestaurantResponse<>(200, "Success", data);
    }

    public static <T> RestaurantResponse<T> success(String message, T data) {
        return new RestaurantResponse<>(200, message, data);
    }

    public static <T> RestaurantResponse<T> error(int status, String message) {
        return new RestaurantResponse<>(status, message, null);
    }
}

