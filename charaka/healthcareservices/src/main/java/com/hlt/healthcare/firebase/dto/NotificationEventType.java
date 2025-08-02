package com.hlt.healthcare.firebase.dto;

import java.util.Map;

public enum NotificationEventType {

    ORDER_ASSIGNED_PARTNER(
            "New Order Assigned",
            "You have been assigned a new delivery for order #{orderNumber} from {restaurantName}. Total: ₹{totalAmount}."
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
