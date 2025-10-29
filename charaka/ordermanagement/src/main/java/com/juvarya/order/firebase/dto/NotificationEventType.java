package com.juvarya.order.firebase.dto;

import java.util.Map;

public enum NotificationEventType {

    ORDER_PLACED_CUSTOMER(
            "Order Confirmed ",
            "Your order from {restaurantName} has been placed successfully. Total amount: ₹{totalAmount}."
    ),
    OTP_TRIGGERED_USER(
            "OTP Verification",
            "Your OTP is {otp}. Please use it to verify your mobile number {primaryContact}."
    ),

    ORDER_PAYMENT_SUCCESS_BUT_FAILED(
            "Order Not Completed",
            "Your order could not be completed. If the amount was deducted, it will be refunded within 1 hour."
    ),
    ORDER_REJECTED_BY_RESTAURANT(
            "Oops! Order Couldn't Be Completed",
            "Looks like the restaurant had to cancel your order — probably out of stock or under pressure! Don't worry, your full refund is on the way and should hit your account within 2 hours. Thanks for rolling with us!"
    ),

    ORDER_ACCEPTED_BY_RESTAURANT(
            "Kitchen's Heating Up 🔥",
            "Boom! Your order is in the kitchen now — it'll be ready in about {prepTime} minutes \uD83D\uDEB4\uD83D\uDCA8 . Get those taste buds prepped!"
    ),
    ORDER_PLACED_RESTAURANT(
            "New Order Received: #{orderNumber}",
            "You have received a new order from {customerName}. Please prepare it for delivery."
    ),
    ORDER_DELIVERED_CUSTOMER(
            "Order Delivered: #{orderNumber}",
            "Your order from {restaurantName} has been delivered. We hope you enjoyed it!"
    ),
    PAYMENT_SUCCESS_USER(
            "Payment Received",
            "Your payment of ₹{totalAmount} was successful."
    ),

    ORDER_DELIVERED_RESTAURANT(
            "Order Delivered Successfully: #{orderNumber}",
            "You have successfully delivered the order to the customer. Good job!"
    ),
    CART_ABANDONED(
            "Hey! Your cart is waiting 🎁",
            "Looks like you forgot something. Come back and complete your order!"
    ),

    USER_INACTIVE(
            "We miss you 💔",
            "It’s been a while. Let’s get you something delicious today!"
    ),

    PAYMENT_RECEIVED_RESTAURANT(
            "Payment Received: #{orderNumber}",
            "Payment for order #{orderNumber} has been received successfully. Amount: ₹{totalAmount}."
    );


    private final String titleTemplate;
    private final String bodyTemplate;

    NotificationEventType(String titleTemplate, String bodyTemplate) {
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
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
