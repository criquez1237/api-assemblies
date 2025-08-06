package com.assembliestore.api.module.sale.domain.entity;

public enum OrderStatus {
    PROCESSING("Processing"),
    CONFIRMED("Confirmed"),
    PAYMENT_FAILED("Payment Failed"),
    EXPIRED("Expired"),
    PREPARING("Preparing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return PROCESSING; // default
    }
}
