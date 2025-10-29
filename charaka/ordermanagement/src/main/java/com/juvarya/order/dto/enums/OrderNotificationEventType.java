package com.juvarya.order.dto.enums;

import java.util.Map;

/**
 * Enum defining different types of order-related notifications.
 * Each type has a title template and body template that can be customized with parameters.
 */
public enum OrderNotificationEventType {

    ORDER_PLACED(
            "Order Placed Successfully",
            "Your order #{orderNumber} has been placed successfully. We'll notify you when it's confirmed."
    ),
    ORDER_CONFIRMED(
            "Order Confirmed",
            "Your order #{orderNumber} has been confirmed. The restaurant is preparing your food."
    ),
    ORDER_PREPARING(
            "Order Being Prepared",
            "Your order #{orderNumber} is now being prepared. It will be ready soon."
    ),
    ORDER_READY_FOR_PICKUP(
            "Order Ready for Pickup",
            "Your order #{orderNumber} is ready for pickup. Please collect it from the restaurant."
    ),
    ORDER_OUT_FOR_DELIVERY(
            "Order Out for Delivery",
            "Your order #{orderNumber} is on its way to you. It will arrive shortly."
    ),
    ORDER_DELIVERED(
            "Order Delivered",
            "Your order #{orderNumber} has been delivered. Enjoy your meal!"
    ),
    ORDER_CANCELLED(
            "Order Cancelled",
            "Your order #{orderNumber} has been cancelled. Please contact support if you have any questions."
    ),
    
    // Restaurant notifications
    RESTAURANT_NEW_ORDER(
            "New Order Received",
            "You have received a new order #{orderNumber}. Please confirm it as soon as possible."
    ),
    RESTAURANT_ORDER_CANCELLED(
            "Order Cancelled",
            "Order #{orderNumber} has been cancelled by the customer."
    );

    private final String titleTemplate;
    private final String bodyTemplate;

    OrderNotificationEventType(String titleTemplate, String bodyTemplate) {
        this.titleTemplate = titleTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public String getTitle(Map<String, String> params) {
        return replacePlaceholders(titleTemplate, params);
    }

    public String getBody(Map<String, String> params) {
        return replacePlaceholders(bodyTemplate, params);
    }

    private String replacePlaceholders(String template, Map<String, String> params) {
        String result = template;
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                result = result.replace("#{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }
}