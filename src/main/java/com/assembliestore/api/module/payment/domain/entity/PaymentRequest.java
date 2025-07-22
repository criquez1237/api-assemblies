package com.assembliestore.api.module.payment.domain.entity;

public class PaymentRequest {
    private Long amount;
    private String currency;
    private String orderId; // Agregar orderId

    public PaymentRequest(Long amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public PaymentRequest(Long amount, String currency, String orderId) {
        this.amount = amount;
        this.currency = currency;
        this.orderId = orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
