package com.assembliestore.api.service.payment;

public record CreatePaymentRequestDto(
    Long amount,
    String currency,
    String description,
    String orderId  // Agregar el ID de la orden
) {

}
