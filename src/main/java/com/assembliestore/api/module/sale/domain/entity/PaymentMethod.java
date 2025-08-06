package com.assembliestore.api.module.sale.domain.entity;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    CASH("Cash"),
    BANK_TRANSFER("Bank Transfer"),
    PAYPAL("PayPal"),
    CASH_ON_DELIVERY("Cash on Delivery");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentMethod fromValue(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equalsIgnoreCase(value) || method.name().equalsIgnoreCase(value)) {
                return method;
            }
        }
        return CASH_ON_DELIVERY; // default
    }
}
