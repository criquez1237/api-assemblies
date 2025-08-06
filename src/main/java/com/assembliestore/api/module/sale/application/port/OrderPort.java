package com.assembliestore.api.module.sale.application.port;

import com.assembliestore.api.module.sale.domain.entity.Order;
import com.assembliestore.api.module.sale.domain.entity.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderPort {
    
    /**
     * Crear una nueva orden
     */
    Order createOrder(Order order);
    
    /**
     * Buscar orden por ID
     */
    Optional<Order> findOrderById(String orderId);
    
    /**
     * Buscar todas las órdenes
     */
    List<Order> findAllOrders();
    
    /**
     * Buscar órdenes por usuario
     */
    List<Order> findOrdersByUserId(String userId);
    
    /**
     * Buscar órdenes por estado
     */
    List<Order> findOrdersByStatus(OrderStatus status);
    
    /**
     * Actualizar estado de orden
     */
    Order updateOrderStatus(String orderId, OrderStatus newStatus);
    
    /**
     * Actualizar orden completa
     */
    Order updateOrder(Order order);
    
    /**
     * Marcar orden como eliminada (soft delete)
     */
    void deleteOrder(String orderId);
}
