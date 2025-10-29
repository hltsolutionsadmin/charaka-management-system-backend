package com.juvarya.order.dto.response;

import com.juvarya.order.dto.OrderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String message;
    private OrderDTO order;
    private List<OrderDTO> orders;
    private boolean success;
    private String errorMessage;


    public static OrderResponse single(String message, OrderDTO order) {
        OrderResponse response = new OrderResponse();
        response.setMessage(message);
        response.setOrder(order);
        response.setSuccess(true);
        return response;
    }

    public static OrderResponse list(String message, List<OrderDTO> orders) {
        OrderResponse response = new OrderResponse();
        response.setMessage(message);
        response.setOrders(orders);
        response.setSuccess(true);
        return response;
    }

    public static OrderResponse message(String message) {
        OrderResponse response = new OrderResponse();
        response.setMessage(message);
        response.setSuccess(true);
        return response;
    }

    public static OrderResponse error(String errorMessage) {
        OrderResponse response = new OrderResponse();
        response.setErrorMessage(errorMessage);
        response.setSuccess(false);
        return response;
    }
}
