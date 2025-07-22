package com.assembliestore.api.service.payment;

public record CreatePaymentResponseDto(
    String clientSecret  // Ahora contiene la URL de Stripe Checkout
) {

}
