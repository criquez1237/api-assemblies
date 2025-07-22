package com.assembliestore.api.service.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.assembliestore.api.module.sale.application.service.OrderService;
import com.assembliestore.api.module.sale.domain.entity.Order;
import com.assembliestore.api.module.sale.domain.entity.OrderStatus;
import com.assembliestore.api.service.realtime.service.RealtimeNotificationService;
import com.assembliestore.api.module.product.application.service.StockService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private RealtimeNotificationService notificationService;
    
    @Autowired
    private StockService stockService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Verificar la firma del webhook
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.error("Invalid signature for webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error parsing webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing webhook");
        }

        // Manejar el evento
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "checkout.session.async_payment_succeeded":
                handleCheckoutSessionCompleted(event);
                break;
            case "checkout.session.async_payment_failed":
                handleCheckoutSessionFailed(event);
                break;
            case "checkout.session.expired":
                handleCheckoutSessionExpired(event);
                break;
            case "charge.dispute.created":
                handleRefund(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailed(event);
                break;
            default:
                logger.info("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook handled");
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<String> createRefund(@PathVariable String orderId) {
        try {
            // Restaurar el stock antes de cambiar el estado
            try {
                orderService.restoreStockForOrder(orderId);
                logger.info("Stock restored for refunded order: {}", orderId);
            } catch (Exception e) {
                logger.error("Failed to restore stock for refund of order {}: {}", orderId, e.getMessage());
                // Continuar con el reembolso aunque falle la restauración de stock
            }
            
            // Cambiar el estado de la orden a REFUNDED
            orderService.updateOrderStatus(orderId, OrderStatus.REFUNDED);
            logger.info("Order {} status updated to REFUNDED", orderId);
            
            // Enviar notificación al usuario
            try {
                // Obtener la orden para conseguir el userId
                Order order = orderService.findOrderById(orderId).orElse(null);
                if (order != null && order.getUserId() != null) {
                    // Enviar notificación sobre cambio de estado
                    notificationService.sendOrderStatusUpdate(
                        orderId,
                        "CONFIRMED",
                        "REFUNDED", 
                        order.getUserId()
                    );
                    
                    // También enviar una notificación más específica sobre el reembolso
                    Map<String, Object> refundData = new HashMap<>();
                    refundData.put("orderId", orderId);
                    refundData.put("paymentStatus", "REFUNDED");
                    refundData.put("amount", order.getTotal().toString());
                    refundData.put("timestamp", System.currentTimeMillis());
                    refundData.put("userId", order.getUserId());
                    
                    notificationService.sendNotification(
                        new com.assembliestore.api.service.realtime.dto.NotificationMessage(
                            "PAYMENT_REFUNDED", 
                            "Payment Refunded", 
                            "Your payment for order #" + orderId + " has been refunded.", 
                            refundData,
                            "CLIENT"
                        )
                    );
                    
                    logger.info("Refund notification sent to user: {}", order.getUserId());
                }
            } catch (Exception e) {
                logger.error("Failed to send notification for refund of order {}: {}", orderId, e.getMessage());
                // Continuar aunque falle el envío de notificación
            }
            
            return ResponseEntity.ok("Refund processed successfully");
        } catch (Exception e) {
            logger.error("Error processing refund for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing refund: " + e.getMessage());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                logger.error("Session is null in checkout.session.completed event");
                return;
            }

            // Obtener el orderId desde los metadata
            String orderId = session.getMetadata().get("order_id");
            if (orderId == null || orderId.isEmpty()) {
                logger.error("Order ID not found in session metadata");
                return;
            }

            logger.info("Payment completed for order: {}", orderId);
            logger.info("Payment status: {}", session.getPaymentStatus());
            logger.info("Session ID: {}", session.getId());

            // Actualizar el estado de la orden a CONFIRMED
            try {
                orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED);
                logger.info("Order {} status updated to CONFIRMED", orderId);
                
                // Obtener la orden para conseguir el userId y enviar notificación
                Order order = orderService.findOrderById(orderId).orElse(null);
                if (order != null && order.getUserId() != null) {
                    // Enviar notificación al usuario específico
                    notificationService.sendOrderStatusUpdate(
                        orderId,
                        "PROCESSING",
                        "CONFIRMED", 
                        order.getUserId()
                    );
                    
                    // También enviar una notificación más específica sobre el pago
                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("orderId", orderId);
                    paymentData.put("paymentStatus", "SUCCESS");
                    paymentData.put("amount", order.getTotal().toString());
                    paymentData.put("timestamp", System.currentTimeMillis());
                    paymentData.put("userId", order.getUserId());
                    
                    notificationService.sendNotification(
                        new com.assembliestore.api.service.realtime.dto.NotificationMessage(
                            "PAYMENT_SUCCESS", 
                            "Payment Successful", 
                            "Your payment for order #" + orderId + " was successful", 
                            paymentData,
                            "CLIENT"
                        )
                    );
                    
                    logger.info("Payment success notification sent to user: {}", order.getUserId());
                }
                
            } catch (Exception e) {
                logger.error("Failed to update order {} status to CONFIRMED: {}", orderId, e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error handling checkout.session.completed: {}", e.getMessage());
        }
    }

    private void handleCheckoutSessionFailed(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                logger.error("Session is null in checkout.session.async_payment_failed event");
                return;
            }
            
            // Obtener el orderId desde los metadata
            String orderId = session.getMetadata().get("order_id");
            if (orderId == null || orderId.isEmpty()) {
                logger.error("Order ID not found in session metadata");
                return;
            }

            logger.info("Payment failed for order: {}", orderId);
            logger.info("Payment status: {}", session.getPaymentStatus());

            // Actualizar el estado de la orden a PAYMENT_FAILED
            try {
                orderService.updateOrderStatus(orderId, OrderStatus.PAYMENT_FAILED);
                logger.info("Order {} status updated to PAYMENT_FAILED", orderId);
                
                // Restaurar el stock
                stockService.restoreStockForOrder(orderId);
                logger.info("Stock restored for order: {}", orderId);
                
                // Obtener la orden para conseguir el userId y enviar notificación
                Order order = orderService.findOrderById(orderId).orElse(null);
                if (order != null && order.getUserId() != null) {
                    // Enviar notificación al usuario específico sobre cambio de estado
                    notificationService.sendOrderStatusUpdate(
                        orderId,
                        "PROCESSING",
                        "PAYMENT_FAILED", 
                        order.getUserId()
                    );
                    
                    // También enviar una notificación más específica sobre el fallo del pago
                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("orderId", orderId);
                    paymentData.put("paymentStatus", "FAILED");
                    paymentData.put("amount", order.getTotal().toString());
                    paymentData.put("timestamp", System.currentTimeMillis());
                    paymentData.put("failureReason", "Payment session failed");
                    paymentData.put("userId", order.getUserId());
                    
                    notificationService.sendNotification(
                        new com.assembliestore.api.service.realtime.dto.NotificationMessage(
                            "PAYMENT_FAILED", 
                            "Payment Failed", 
                            "Your payment for order #" + orderId + " has failed", 
                            paymentData,
                            "CLIENT"
                        )
                    );
                    
                    logger.info("Payment failure notification sent to user: {}", order.getUserId());
                }
            } catch (Exception e) {
                logger.error("Failed to update order {} status or restore stock: {}", orderId, e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error handling checkout.session.async_payment_failed: {}", e.getMessage());
        }
    }    private void handleCheckoutSessionExpired(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                logger.error("Session is null in checkout.session.expired event");
                return;
            }

            // Obtener el orderId desde los metadata
            String orderId = session.getMetadata().get("order_id");
            if (orderId == null || orderId.isEmpty()) {
                logger.error("Order ID not found in session metadata");
                return;
            }

            logger.info("Payment session expired for order: {}", orderId);

            // Actualizar el estado de la orden a EXPIRED
            try {
                orderService.updateOrderStatus(orderId, OrderStatus.EXPIRED);
                logger.info("Order {} status updated to EXPIRED", orderId);
                
                // Restaurar el stock
                stockService.restoreStockForOrder(orderId);
                logger.info("Stock restored for order: {}", orderId);
                
                // Obtener la orden para conseguir el userId y enviar notificación
                Order order = orderService.findOrderById(orderId).orElse(null);
                if (order != null && order.getUserId() != null) {
                    // Enviar notificación al usuario específico sobre cambio de estado
                    notificationService.sendOrderStatusUpdate(
                        orderId,
                        "PROCESSING",
                        "EXPIRED", 
                        order.getUserId()
                    );
                    
                    // También enviar una notificación más específica sobre la expiración del pago
                    Map<String, Object> paymentData = new HashMap<>();
                    paymentData.put("orderId", orderId);
                    paymentData.put("paymentStatus", "EXPIRED");
                    paymentData.put("amount", order.getTotal().toString());
                    paymentData.put("timestamp", System.currentTimeMillis());
                    paymentData.put("userId", order.getUserId());
                    
                    notificationService.sendNotification(
                        new com.assembliestore.api.service.realtime.dto.NotificationMessage(
                            "PAYMENT_EXPIRED", 
                            "Payment Session Expired", 
                            "Your payment session for order #" + orderId + " has expired. Please try again if you wish to complete this order.", 
                            paymentData,
                            "CLIENT"
                        )
                    );
                    
                    logger.info("Payment expiration notification sent to user: {}", order.getUserId());
                }
            } catch (Exception e) {
                logger.error("Failed to update order {} status or restore stock: {}", orderId, e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error handling checkout.session.expired: {}", e.getMessage());
        }
    }

    private void handleRefund(Event event) {
        try {
            // Para disputes/chargebacks, necesitamos obtener la información del charge
            logger.info("Refund/Dispute created for payment");
            logger.info("Event type: {}", event.getType());
            logger.info("Event ID: {}", event.getId());
            
            // Para manejar correctamente los reembolsos, necesitaríamos más información
            // sobre cómo asociar el evento con una orden específica.
            // Por ahora, solo registramos el evento.
            
        } catch (Exception e) {
            logger.error("Error handling refund/dispute: {}", e.getMessage());
        }
    }

    private void handlePaymentFailed(Event event) {
        try {
            logger.info("Payment failed event received");
            logger.info("Event type: {}", event.getType());
            logger.info("Event ID: {}", event.getId());
            
            // Para manejar correctamente los pagos fallidos que no sean de checkout sessions,
            // necesitaríamos más información sobre cómo asociar el evento con una orden específica.
            // Los payment_intent failures normalmente se manejan a través de checkout.session.async_payment_failed
            // Por ahora, solo registramos el evento.
            
        } catch (Exception e) {
            logger.error("Error handling payment failed: {}", e.getMessage());
        }
    }
}
