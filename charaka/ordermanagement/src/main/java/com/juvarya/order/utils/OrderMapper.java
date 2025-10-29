package com.juvarya.order.utils;

import com.juvarya.order.dto.OrderDTO;
import com.juvarya.order.dto.OrderItemDTO;
import com.juvarya.order.entity.OrderItemModel;
import com.juvarya.order.entity.OrderModel;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toDto(OrderModel order) {
        if (order == null) return null;

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setBusinessId(order.getBusinessId());
        dto.setBusinessName(order.getBusinessName());
        dto.setShippingAddressId(order.getShippingAddressId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentTransactionId(order.getPaymentTransactionId());
        dto.setCreatedDate(order.getCreatedDate());
        dto.setUpdatedDate(order.getUpdatedDate());

        if (order.getOrderItems() != null) {
            dto.setOrderItems(toItemDtoList(order.getOrderItems()));
        }

        return dto;
    }

    public static List<OrderDTO> toDtoList(List<OrderModel> orders) {
        return orders.stream().map(OrderMapper::toDto).collect(Collectors.toList());
    }

    private static List<OrderItemDTO> toItemDtoList(List<OrderItemModel> items) {
        return items.stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProductId());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setEntryNumber(item.getEntryNumber());
            // Optional: set product name and media here if available
            return itemDTO;
        }).collect(Collectors.toList());
    }
}