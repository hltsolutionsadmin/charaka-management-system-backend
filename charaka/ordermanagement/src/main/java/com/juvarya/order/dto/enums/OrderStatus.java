package com.juvarya.order.dto.enums;

import java.util.List;

public enum OrderStatus {

    PLACED("Order Placed"),
    ACCEPTED("Order Accepted"),
    REJECTED("Order Rejected"),
    CONFIRMED("Order Confirmed"),
    PREPARING("Preparing"),
    DELIVERY_REJECTED("Rejected by Delivery Partner"),
    READY_FOR_PICKUP("Ready for Pickup"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    PICKED_UP("Picked Up"),
    PENDING("Pending"),
    ASSIGNED("Assigned"),
    RETURNED("Returned"),
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
        return List.of(DELIVERED);   // Only Delivered orders are valid for review but its future enhancement
    }
}
