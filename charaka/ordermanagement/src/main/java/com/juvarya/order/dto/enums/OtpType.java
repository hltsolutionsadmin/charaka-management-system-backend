package com.juvarya.order.dto.enums;

public enum OtpType {
    DELIVERY,
    RETURN;

    public static OtpType from(String type) {
        if (type == null) {
            return DELIVERY; // default type
        }
        try {
            return OtpType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid OTP type: " + type);
        }
    }
}
