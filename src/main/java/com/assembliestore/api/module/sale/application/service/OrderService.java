package com.assembliestore.api.module.sale.application.service;

import com.assembliestore.api.module.sale.application.dto.response.OrderPaymentResponse;
import com.assembliestore.api.module.sale.application.port.OrderPort;
import com.assembliestore.api.module.sale.domain.entity.Order;
import com.assembliestore.api.module.sale.domain.entity.OrderProduct;
import com.assembliestore.api.module.sale.domain.entity.OrderStatus;
import com.assembliestore.api.module.payment.domain.port.PaymentPort;
import com.assembliestore.api.module.sale.domain.entity.PaymentMethod;
import com.assembliestore.api.module.payment.domain.entity.PaymentRequest;
import com.assembliestore.api.module.product.domain.port.StockPort;
import com.assembliestore.api.module.user.domain.repository.UserRepository;
import com.assembliestore.api.module.user.domain.entities.User;
import com.assembliestore.api.service.email.EmailService;
import com.assembliestore.api.service.email.dto.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderPort orderPort;

    @Autowired
    private PaymentPort paymentPort;
    
    @Autowired
    private StockPort stockPort;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    public OrderPaymentResponse createOrder(Order order) {
        // Generate UUID if not present
        if (order.getId() == null || order.getId().isEmpty()) {
            order.setId(UUID.randomUUID().toString());
        }
        
        // Set initial status
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PROCESSING);
        }
        
        // Calculate total if not set
        order.calculateTotal();
        
        // Verificar y reducir stock antes de procesar el pago
        Map<String, Integer> productQuantities = new HashMap<>();
        if (order.getProducts() != null) {
            for (OrderProduct orderProduct : order.getProducts()) {
                productQuantities.put(orderProduct.getProductId(), orderProduct.getQuantity());
            }
        }
        
        // Verificar disponibilidad de stock
        if (!productQuantities.isEmpty()) {
            Map<String, Boolean> stockAvailability = stockPort.checkStockAvailability(productQuantities);
            
            // Verificar si todos los productos tienen suficiente stock
            for (Map.Entry<String, Boolean> entry : stockAvailability.entrySet()) {
                if (!entry.getValue()) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + entry.getKey());
                }
            }
            
            // Reducir el stock
            boolean stockReduced = stockPort.reduceStock(productQuantities);
            if (!stockReduced) {
                throw new RuntimeException("Error al reducir el stock de los productos");
            }
        }

        String clientSecret = null;
        if (order.getPaymentMethod() == PaymentMethod.CREDIT_CARD || order.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
            PaymentRequest paymentRequest = new PaymentRequest(order.getTotal().longValue(), "usd", order.getId()); // Pasar el orderId
            clientSecret = paymentPort.createPayment(paymentRequest); // Ahora esto devuelve una URL de Stripe Checkout
        }
        
        Order savedOrder = orderPort.createOrder(order);
        return new OrderPaymentResponse(savedOrder, clientSecret); // clientSecret ahora contiene la URL de pago
    }

    public Optional<Order> findOrderById(String orderId) {
        return orderPort.findOrderById(orderId);
    }

    public List<Order> findAllOrders() {
        return orderPort.findAllOrders();
    }

    public List<Order> findOrdersByUserId(String userId) {
        return orderPort.findOrdersByUserId(userId);
    }

    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderPort.findOrdersByStatus(status);
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        Optional<Order> existingOrder = orderPort.findOrderById(orderId);
        if (existingOrder.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        // Validate status transition
        validateStatusTransition(existingOrder.get().getStatus(), newStatus);
        return orderPort.updateOrderStatus(orderId, newStatus);
    }

    public Order updateOrder(Order order) {
        Optional<Order> existingOrder = orderPort.findOrderById(order.getId());
        if (existingOrder.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + order.getId());
        }
        // Recalculate total
        order.calculateTotal();
        return orderPort.updateOrder(order);
    }

    public void deleteOrder(String orderId) {
        Optional<Order> existingOrder = orderPort.findOrderById(orderId);
        if (existingOrder.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        
        Order order = existingOrder.get();
        OrderStatus currentStatus = order.getStatus();
        
        // Only allow deletion in certain statuses
        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot delete an order that has already been shipped or delivered");
        }
        
        // Si la orden está en estado PROCESSING, restaurar el stock ya que se había reducido
        // Para PAYMENT_FAILED y EXPIRED, el stock ya debería estar restaurado, pero verificamos
        if (currentStatus == OrderStatus.PROCESSING) {
            try {
                restoreStockForOrder(orderId);
                System.out.println("Stock restored for deleted order: " + orderId);
            } catch (Exception e) {
                System.err.println("Failed to restore stock for deleted order " + orderId + ": " + e.getMessage());
                // Continuar con la eliminación aunque falle la restauración de stock
            }
        }
        
        orderPort.deleteOrder(orderId);
    }
    
    public void restoreStockForOrder(String orderId) {
        Optional<Order> orderOpt = orderPort.findOrderById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            Map<String, Integer> productQuantities = new HashMap<>();
            if (order.getProducts() != null) {
                for (OrderProduct orderProduct : order.getProducts()) {
                    productQuantities.put(orderProduct.getProductId(), orderProduct.getQuantity());
                }
            }
            
            if (!productQuantities.isEmpty()) {
                stockPort.restoreStock(productQuantities);
                System.out.println("Stock restaurado para la orden: " + orderId);
            }
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Business rules for status transitions
        switch (currentStatus) {
            case PROCESSING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED && 
                    newStatus != OrderStatus.PAYMENT_FAILED && newStatus != OrderStatus.EXPIRED) {
                    throw new RuntimeException("From PROCESSING you can only change to CONFIRMED, CANCELLED, PAYMENT_FAILED or EXPIRED");
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.REFUNDED) {
                    throw new RuntimeException("From CONFIRMED you can only change to PREPARING, CANCELLED or REFUNDED");
                }
                break;
            case PAYMENT_FAILED:
            case EXPIRED:
                // Estas órdenes pueden ser canceladas o se puede intentar el pago nuevamente
                if (newStatus != OrderStatus.CANCELLED && newStatus != OrderStatus.PROCESSING) {
                    throw new RuntimeException("From " + currentStatus.getValue() + " you can only change to CANCELLED or PROCESSING (retry)");
                }
                break;
            case PREPARING:
                if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.CANCELLED) {
                    throw new RuntimeException("From PREPARING you can only change to SHIPPED or CANCELLED");
                }
                break;
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED) {
                    throw new RuntimeException("From SHIPPED you can only change to DELIVERED");
                }
                break;
            case DELIVERED:
                if (newStatus != OrderStatus.REFUNDED) {
                    throw new RuntimeException("From DELIVERED you can only change to REFUNDED");
                }
                break;
            case CANCELLED:
            case REFUNDED:
                throw new RuntimeException("Cannot change status of an order in " + currentStatus.getValue());
            default:
                throw new RuntimeException("Unrecognized current status: " + currentStatus);
        }
    }

    public Order cancelOrder(String orderId, String userId) {
        // Buscar la orden
        Optional<Order> orderOpt = orderPort.findOrderById(orderId);
        if (!orderOpt.isPresent()) {
            throw new RuntimeException("Orden no encontrada");
        }
        
        Order order = orderOpt.get();
        
        // Verificar que la orden pertenezca al usuario (si no es admin)
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para cancelar esta orden");
        }
        
        // Verificar que la orden se pueda cancelar
        if (!canBeCancelled(order.getStatus())) {
            throw new RuntimeException("La orden no puede ser cancelada en su estado actual: " + order.getStatus().getValue());
        }
        
        // Restaurar stock si la orden había reducido stock
        if (order.getStatus() == OrderStatus.PROCESSING || order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PREPARING) {
            if (order.getProducts() != null) {
                Map<String, Integer> productQuantities = new HashMap<>();
                for (OrderProduct orderProduct : order.getProducts()) {
                    productQuantities.put(orderProduct.getProductId(), orderProduct.getQuantity());
                }
                stockPort.restoreStock(productQuantities);
            }
        }
        
        // Cambiar estado a cancelado
        order.setStatus(OrderStatus.CANCELLED);
        
        // Guardar la orden actualizada
        Order updatedOrder = orderPort.updateOrder(order);
        
        // Enviar email de cancelación
        try {
            sendCancellationEmail(updatedOrder);
        } catch (Exception e) {
            logger.warn("No se pudo enviar el email de cancelación para la orden: {}. Error: {}", 
                       updatedOrder.getId(), e.getMessage());
            // No fallar la cancelación si el email no se puede enviar
        }
        
        return updatedOrder;
    }
    
    private boolean canBeCancelled(OrderStatus status) {
        // Una orden puede ser cancelada si está en estos estados
        return status == OrderStatus.PROCESSING ||
               status == OrderStatus.CONFIRMED ||
               status == OrderStatus.PREPARING ||
               status == OrderStatus.PAYMENT_FAILED ||
               status == OrderStatus.EXPIRED;
    }

    private void sendCancellationEmail(Order order) {
        try {
            // Obtener información del usuario
            Optional<User> userOpt = userRepository.findById(order.getUserId());
            if (!userOpt.isPresent()) {
                logger.warn("Usuario no encontrado para la orden {}: {}", order.getId(), order.getUserId());
                return;
            }

            User user = userOpt.get();
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getPerfil().getNames() + " " + user.getPerfil().getSurnames());
            variables.put("orderId", order.getId());

            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(user.getEmail());
            emailRequest.setSubject("Cancelación de Orden - " + order.getId() + " - Assemblies Store");
            emailRequest.setTemplateName("order-cancellation");
            emailRequest.setVariables(variables);

            emailService.sendEmail(emailRequest);
            
            logger.info("Email de cancelación enviado exitosamente para la orden: {} al usuario: {}", 
                       order.getId(), user.getEmail());
        } catch (Exception e) {
            logger.error("Error al enviar email de cancelación para la orden {}: {}", order.getId(), e.getMessage());
            throw e;
        }
    }
}
