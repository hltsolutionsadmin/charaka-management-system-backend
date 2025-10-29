package com.juvarya.order.dto.notification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.juvarya.order.dto.enums.OrderNotificationEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * DTO for order-related notifications.
 * Contains information needed to send a notification about an order.
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderNotification {
    private String title;
    private String body;
    private Long userId;
    private Long restaurantId;
    private Long orderId;
    private String orderNumber;

    /**
     * Build a notification for a user
     * 
     * @param userId The ID of the user to notify
     * @param orderId The ID of the order
     * @param orderNumber The order number
     * @param eventType The type of notification event
     * @param params Additional parameters for the notification templates
     * @return A notification object
     */
    @JsonIgnore
    public static OrderNotification buildUserNotification(
            final Long userId, 
            final Long orderId,
            final String orderNumber,
            final OrderNotificationEventType eventType, 
            final Map<String, String> params) {
        
        OrderNotification notification = new OrderNotification();
        notification.setUserId(userId);
        notification.setOrderId(orderId);
        notification.setOrderNumber(orderNumber);
        
        // Add orderNumber to params if not already present
        if (params != null && !params.containsKey("orderNumber")) {
            params.put("orderNumber", orderNumber);
        }
        
        notification.setTitle(eventType.getTitle(params));
        notification.setBody(eventType.getBody(params));
        
        return notification;
    }

    /**
     * Build a notification for a restaurant
     * 
     * @param restaurantId The ID of the restaurant to notify
     * @param orderId The ID of the order
     * @param orderNumber The order number
     * @param eventType The type of notification event
     * @param params Additional parameters for the notification templates
     * @return A notification object
     */
    @JsonIgnore
    public static OrderNotification buildRestaurantNotification(
            final Long restaurantId, 
            final Long orderId,
            final String orderNumber,
            final OrderNotificationEventType eventType, 
            final Map<String, String> params) {
        
        OrderNotification notification = new OrderNotification();
        notification.setRestaurantId(restaurantId);
        notification.setOrderId(orderId);
        notification.setOrderNumber(orderNumber);
        
        // Add orderNumber to params if not already present
        if (params != null && !params.containsKey("orderNumber")) {
            params.put("orderNumber", orderNumber);
        }
        
        notification.setTitle(eventType.getTitle(params));
        notification.setBody(eventType.getBody(params));
        
        return notification;
    }
}