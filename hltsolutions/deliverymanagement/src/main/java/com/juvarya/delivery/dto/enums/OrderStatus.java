package com.juvarya.delivery.dto.enums;

import java.util.List;

public enum OrderStatus {

    PLACED("Order Placed"),
    ACCEPTED("Order Accepted"),
    REJECTED("Order Rejected"),
    CONFIRMED("Order Confirmed"),
    PREPARING("Preparing"),
    READY_FOR_PICKUP("Ready for Pickup"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    ASSIGNED("Assigned"),
    PICKED_UP("Picked Up"),
    RETURN_TO_RESTAURANT_PENDING("RETURN TO RESTAURANT PENDING"),
    DELIVERY_FAILED("DELIVERY_FAILED"),
    RETURNED_TO_RESTAURANT("RETURNED TO RESTAURANT"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Returns a list of statuses that are considered valid for customer review submission.
     */
    public static List<OrderStatus> validForReview() {
        return List.of(CONFIRMED);   // Only Delivered orders are valid for review but its future enhancement
    }
}
