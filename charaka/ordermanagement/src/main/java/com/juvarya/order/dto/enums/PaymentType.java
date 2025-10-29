package com.juvarya.order.dto.enums;

public enum PaymentType {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    ONLINE;

    @Override
    public String toString() {
        return name(); // Return the name of the enum as string
    }
}