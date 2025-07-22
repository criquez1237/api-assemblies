package com.assembliestore.api.service.realtime.service;

import com.assembliestore.api.service.realtime.dto.NotificationMessage;
import com.assembliestore.api.service.realtime.dto.StockUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealtimeNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RealtimeNotificationService.class);

    @Autowired
    private WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper;

    public RealtimeNotificationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Enviar notificación de stock a usuarios MANAGEMENT y ADMIN
     */
    public void sendStockUpdate(StockUpdateMessage stockUpdate) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(stockUpdate);
            
            // Enviar a conexiones de tipo STOCK (para clientes conectados específicamente al canal de stock)
            sessionManager.broadcastToType("STOCK", jsonMessage);
            
            // También enviar a conexiones NOTIFICATIONS (donde estarán MANAGEMENT y ADMIN)
            sessionManager.broadcastToType("NOTIFICATIONS", jsonMessage);
            
            // Enviar al canal específico de stock-updates
            sessionManager.broadcastToChannel("stock-updates", jsonMessage);
            
            // Enviar también al canal de alertas de inventario para MANAGEMENT
            sessionManager.broadcastToChannel("inventory-alerts", jsonMessage);
            
            logger.info("Stock update sent to MANAGEMENT/ADMIN for product: {} - Stock: {}", 
                       stockUpdate.getProductId(), stockUpdate.getCurrentStock());
            
        } catch (Exception e) {
            logger.error("Error sending stock update: {}", e.getMessage());
        }
    }

    /**
     * Enviar notificación general a diferentes roles
     */
    public void sendNotification(NotificationMessage notification) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(notification);
            
            // Determinar a qué tipo de conexión enviar
            String targetType = determineTargetType(notification.getTargetRole());
            
            if ("ALL".equals(notification.getTargetRole())) {
                // Enviar a todas las conexiones de notificaciones
                sessionManager.broadcastToType("NOTIFICATIONS", jsonMessage);
                sessionManager.broadcastToType("GENERAL", jsonMessage);
            } else {
                // Enviar a tipo específico
                sessionManager.broadcastToType(targetType, jsonMessage);
            }
            
            // También enviar a canal específico si está definido
            if (notification.getTargetChannel() != null) {
                sessionManager.broadcastToChannel(notification.getTargetChannel(), jsonMessage);
            }
            
            logger.info("Notification sent - Type: {} - Target: {} - Title: {}", 
                       notification.getType(), notification.getTargetRole(), notification.getTitle());
            
        } catch (Exception e) {
            logger.error("Error sending notification: {}", e.getMessage());
        }
    }

    /**
     * Enviar notificación de cambio de estado de orden
     */
    public void sendOrderStatusUpdate(String orderId, String oldStatus, String newStatus, String userId) {
        NotificationMessage notification = new NotificationMessage();
        notification.setType("ORDER_STATUS_UPDATE");
        notification.setTitle("Order Status Updated");
        notification.setMessage("Your order #" + orderId + " status changed from " + oldStatus + " to " + newStatus);
        notification.setTargetRole("CLIENT"); // Principalmente para clientes
        notification.setTargetChannel("order-updates");
        notification.setPriority("MEDIUM");
        
        // Crear datos específicos del pedido
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("oldStatus", oldStatus);
        orderData.put("newStatus", newStatus);
        orderData.put("userId", userId);
        notification.setData(orderData);
        
        sendNotification(notification);
    }

    /**
     * Enviar notificación de producto agotado
     */
    public void sendOutOfStockAlert(String productId, String productName) {
        // Notificación para MANAGEMENT
        NotificationMessage managementNotification = new NotificationMessage();
        managementNotification.setType("OUT_OF_STOCK");
        managementNotification.setTitle("Product Out of Stock");
        managementNotification.setMessage("Product '" + productName + "' is now out of stock");
        managementNotification.setTargetRole("MANAGEMENT");
        managementNotification.setTargetChannel("inventory-alerts");
        managementNotification.setPriority("HIGH");
        
        Map<String, Object> productData = new HashMap<>();
        productData.put("productId", productId);
        productData.put("productName", productName);
        productData.put("action", "restock_required");
        managementNotification.setData(productData);
        
        sendNotification(managementNotification);
        
        // También crear mensaje de stock específico
        StockUpdateMessage stockUpdate = new StockUpdateMessage(productId, productName, 1, 0);
        stockUpdate.setReason("SALE");
        stockUpdate.setChangeType("OUT_OF_STOCK");
        sendStockUpdate(stockUpdate);
    }

    /**
     * Enviar notificación de bienvenida a nuevo usuario
     */
    public void sendWelcomeNotification(String userId, String userName) {
        NotificationMessage notification = new NotificationMessage();
        notification.setType("WELCOME");
        notification.setTitle("Welcome to Assemblies Store");
        notification.setMessage("Welcome " + userName + "! Thanks for joining us.");
        notification.setTargetRole("CLIENT");
        notification.setTargetChannel("user-welcome");
        notification.setPriority("LOW");
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("userName", userName);
        userData.put("isNewUser", true);
        notification.setData(userData);
        
        sendNotification(notification);
    }

    /**
     * Enviar mensaje personalizado a una sesión específica
     */
    public boolean sendPersonalMessage(String sessionId, String title, String message, Object data) {
        try {
            Map<String, Object> personalMessage = new HashMap<>();
            personalMessage.put("type", "PERSONAL_MESSAGE");
            personalMessage.put("title", title);
            personalMessage.put("message", message);
            personalMessage.put("data", data);
            personalMessage.put("timestamp", System.currentTimeMillis());
            
            String jsonMessage = objectMapper.writeValueAsString(personalMessage);
            return sessionManager.sendToSession(sessionId, jsonMessage);
            
        } catch (Exception e) {
            logger.error("Error sending personal message to session {}: {}", sessionId, e.getMessage());
            return false;
        }
    }

    /**
     * Obtener estadísticas de conexiones WebSocket
     */
    public Map<String, Object> getConnectionStats() {
        return sessionManager.getConnectionStats();
    }

    /**
     * Determinar el tipo de conexión basado en el rol objetivo
     */
    private String determineTargetType(String targetRole) {
        if (targetRole == null) {
            return "GENERAL";
        }
        
        switch (targetRole.toUpperCase()) {
            case "MANAGEMENT":
                return "NOTIFICATIONS"; // MANAGEMENT puede recibir tanto stock como notificaciones
            case "ADMIN":
                return "NOTIFICATIONS";
            case "CLIENT":
                return "NOTIFICATIONS";
            default:
                return "GENERAL";
        }
    }

    /**
     * Enviar notificación de stock específicamente a roles MANAGEMENT y ADMIN
     */
    public void sendStockUpdateToManagement(String productId, String productName, int previousStock, int currentStock, String changeType) {
        // Primero, crear y enviar un StockUpdateMessage para compatibilidad con el cliente
        StockUpdateMessage stockUpdate = new StockUpdateMessage(productId, productName, previousStock, currentStock);
        stockUpdate.setChangeType(changeType);
        stockUpdate.setReason("SALE"); // O el reason apropiado
        
        // Enviar el StockUpdateMessage
        sendStockUpdate(stockUpdate);
        
        // También crear mensaje de notificación para MANAGEMENT
        NotificationMessage managementNotification = new NotificationMessage();
        managementNotification.setType("STOCK_UPDATE");
        managementNotification.setTitle("Stock Update");
        managementNotification.setMessage(String.format("Stock for %s changed from %d to %d (%s)", 
            productName, previousStock, currentStock, changeType));
        managementNotification.setTargetRole("MANAGEMENT");
        managementNotification.setTargetChannel("stock-updates");
        managementNotification.setPriority("MEDIUM");
        
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("productId", productId);
        stockData.put("productName", productName);
        stockData.put("previousStock", previousStock);
        stockData.put("currentStock", currentStock);
        stockData.put("stockChange", currentStock - previousStock);
        stockData.put("changeType", changeType);
        managementNotification.setData(stockData);
        
        // Enviar a MANAGEMENT
        sendNotification(managementNotification);
        
        // Crear mensaje similar para ADMIN
        NotificationMessage adminNotification = new NotificationMessage();
        adminNotification.setType("STOCK_UPDATE");
        adminNotification.setTitle("Stock Update");
        adminNotification.setMessage(managementNotification.getMessage());
        adminNotification.setTargetRole("ADMIN");
        adminNotification.setTargetChannel("stock-updates");
        adminNotification.setPriority("MEDIUM");
        adminNotification.setData(stockData);
        
        // Enviar a ADMIN
        sendNotification(adminNotification);
        
        logger.info("Stock update sent to MANAGEMENT and ADMIN for product: {} - Previous: {}, Current: {}", 
                   productName, previousStock, currentStock);
    }
}
