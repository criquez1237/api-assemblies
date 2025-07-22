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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderPort orderPort;

    @Autowired
    private PaymentPort paymentPort;
    
    @Autowired
    private StockPort stockPort;

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
}
