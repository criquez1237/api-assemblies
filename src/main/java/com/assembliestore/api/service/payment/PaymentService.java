package com.assembliestore.api.service.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${app.frontend.success-url:http://localhost:3000/payment/success}")
    private String successUrl;

    @Value("${app.frontend.cancel-url:http://localhost:3000/payment/cancel}")
    private String cancelUrl;

    public CreatePaymentResponseDto createPaymentIntent(CreatePaymentRequestDto request) throws StripeException {
        // Crear parámetros para la sesión de checkout
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .putMetadata("order_id", request.orderId()) // Agregar el ID de la orden en metadata
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(request.currency())
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(request.description() != null ? request.description() : "Order Payment")
                                        .build()
                                )
                                .setUnitAmount(request.amount() * 100L) // Convertir a centavos
                                .build()
                        )
                        .setQuantity(1L)
                        .build()
                )
                .build();

        // Crear la sesión de checkout en Stripe
        Session session = Session.create(params);

        // Devolver la URL de la sesión de checkout
        return new CreatePaymentResponseDto(session.getUrl());
    }

    public boolean processRefund(String sessionId) throws StripeException {
        try {
            // Obtener la sesión de checkout
            Session session = Session.retrieve(sessionId);
            
            if (session.getPaymentIntent() != null) {
                // Crear el reembolso
                com.stripe.model.Refund refund = com.stripe.model.Refund.create(
                    com.stripe.param.RefundCreateParams.builder()
                        .setPaymentIntent(session.getPaymentIntent())
                        .build()
                );
                
                logger.info("Refund created: {}", refund.getId());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error processing refund: {}", e.getMessage());
            throw e;
        }
    }
}
