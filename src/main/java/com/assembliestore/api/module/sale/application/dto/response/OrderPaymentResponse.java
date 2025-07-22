package com.assembliestore.api.module.sale.application.dto.response;

import com.assembliestore.api.module.sale.domain.entity.Order;

public class OrderPaymentResponse {
    private final Order order;
    private final String paymentUrl; // Cambiar de clientSecret a paymentUrl

    public OrderPaymentResponse(Order order, String paymentUrl) {
        this.order = order;
        this.paymentUrl = paymentUrl;
    }

    public Order getOrder() {
        return order;
    }

    public String getPaymentUrl() { // Cambiar getter name
        return paymentUrl;
    }

    // Mantener el getter anterior para compatibilidad
    @Deprecated
    public String getClientSecret() {
        return paymentUrl;
    }
}
