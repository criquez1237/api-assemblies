# WebSocket Implementation - Assemblies Store

## 📡 Endpoints WebSocket Disponibles

### 1. **Stock Updates** (Solo MANAGEMENT)
```
ws://localhost:8081/api/ws/stock
```
- **Propósito**: Recibir actualizaciones de stock en tiempo real
- **Usuarios**: Solo roles MANAGEMENT
- **Datos**: Información de cambios de inventario

### 2. **Notifications** (CLIENT, ADMIN, MANAGEMENT)
```
ws://localhost:8081/api/ws/notifications
```
- **Propósito**: Recibir notificaciones generales
- **Usuarios**: CLIENT, ADMIN, MANAGEMENT
- **Datos**: Notificaciones de pedidos, bienvenida, alertas

### 3. **General** (Todos los usuarios)
```
ws://localhost:8081/api/ws/general
```
- **Propósito**: Canal general para cualquier comunicación
- **Usuarios**: Todos los roles
- **Datos**: Mensajes generales, ping/pong

## 🔌 Cómo Conectarse

### JavaScript Example
```javascript
// Conectar a notificaciones
const wsNotifications = new WebSocket('ws://localhost:8081/api/ws/notifications');

wsNotifications.onopen = function(event) {
    console.log('Connected to notifications channel');
};

wsNotifications.onmessage = function(event) {
    const message = JSON.parse(event.data);
    console.log('Notification received:', message);
    handleNotification(message);
};

wsNotifications.onclose = function(event) {
    console.log('Disconnected from notifications channel');
};

// Conectar a stock updates (solo MANAGEMENT)
const wsStock = new WebSocket('ws://localhost:8081/api/ws/stock');

wsStock.onmessage = function(event) {
    const stockUpdate = JSON.parse(event.data);
    console.log('Stock update:', stockUpdate);
    updateStockDisplay(stockUpdate);
};
```

## 📨 Tipos de Mensajes

### 1. **Stock Update Message**
```json
{
    "productId": "prod_123",
    "productName": "Gaming Mouse",
    "previousStock": 10,
    "currentStock": 5,
    "stockChange": -5,
    "changeType": "DECREASE",
    "reason": "SALE",
    "timestamp": 1701234567890
}
```

### 2. **Notification Message**
```json
{
    "type": "ORDER_STATUS_UPDATE",
    "title": "Order Status Updated",
    "message": "Your order #ORD123 status changed from PROCESSING to CONFIRMED",
    "data": {
        "orderId": "ORD123",
        "oldStatus": "PROCESSING",
        "newStatus": "CONFIRMED",
        "userId": "user_456"
    },
    "targetRole": "CLIENT",
    "targetChannel": "order-updates",
    "timestamp": "2024-01-01T10:30:00",
    "priority": "MEDIUM"
}
```

### 3. **Welcome Message**
```json
{
    "type": "WELCOME",
    "connectionType": "NOTIFICATIONS",
    "message": "Connected successfully to NOTIFICATIONS channel",
    "timestamp": 1701234567890
}
```

## 🎯 Comandos del Cliente

### Ping/Pong
```javascript
// Enviar ping para mantener la conexión
ws.send(JSON.stringify({
    "type": "PING"
}));

// Respuesta esperada:
{
    "type": "PONG",
    "timestamp": 1701234567890
}
```

### Suscribirse a Canal
```javascript
ws.send(JSON.stringify({
    "type": "SUBSCRIBE",
    "channel": "stock-updates"
}));

// Respuesta:
{
    "type": "SUBSCRIPTION_CONFIRMED",
    "channel": "stock-updates",
    "message": "Subscribed to channel: stock-updates"
}
```

### Desuscribirse de Canal
```javascript
ws.send(JSON.stringify({
    "type": "UNSUBSCRIBE",
    "channel": "stock-updates"
}));
```

## 🛠️ API REST para Testing

### Obtener Estadísticas
```http
GET /api/realtime/stats
Authorization: Bearer <token>
```

### Enviar Notificación de Prueba
```http
POST /api/realtime/test-notification
Content-Type: application/json
Authorization: Bearer <token>

{
    "type": "TEST",
    "title": "Test Notification",
    "message": "This is a test notification",
    "targetRole": "CLIENT",
    "priority": "LOW"
}
```

### Enviar Update de Stock de Prueba
```http
POST /api/realtime/test-stock-update
Content-Type: application/json
Authorization: Bearer <token>

{
    "productId": "test_product",
    "productName": "Test Product",
    "previousStock": 10,
    "currentStock": 8,
    "reason": "SALE"
}
```

## 🔐 Seguridad y Roles

### Acceso por Endpoint:
- **`/ws/stock`**: Solo MANAGEMENT
- **`/ws/notifications`**: CLIENT, ADMIN, MANAGEMENT
- **`/ws/general`**: Todos los usuarios

### Tipos de Notificaciones por Rol:
- **CLIENT**: Actualizaciones de pedidos, bienvenida, ofertas
- **ADMIN**: Alertas del sistema, reportes
- **MANAGEMENT**: Stock, inventario, todas las notificaciones

## 📊 Monitoreo

### Logs Disponibles:
- Conexiones establecidas/cerradas
- Mensajes enviados/recibidos
- Errores de transmisión
- Estadísticas de sesiones activas

### Estadísticas en Tiempo Real:
```json
{
    "totalSessions": 15,
    "sessionsByType": {
        "STOCK": 3,
        "NOTIFICATIONS": 10,
        "GENERAL": 2
    },
    "activeChannels": ["stock-updates", "order-updates", "inventory-alerts"],
    "channelSubscribers": {
        "stock-updates": 3,
        "order-updates": 8,
        "inventory-alerts": 2
    }
}
```

## 🚀 Integración con Módulos

### En OrderService (ejemplo):
```java
@Autowired
private RealtimeNotificationService notificationService;

// Cuando cambia el estado de una orden
public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
    Order order = // ... lógica existente
    
    // Enviar notificación en tiempo real
    notificationService.sendOrderStatusUpdate(
        orderId, 
        oldStatus.getValue(), 
        newStatus.getValue(), 
        order.getUserId()
    );
    
    return order;
}
```

### En ProductService (ejemplo):
```java
// Cuando el stock cambia
public void updateProductStock(String productId, int newStock) {
    Product product = // ... lógica existente
    
    // Si se agota el stock
    if (newStock == 0) {
        notificationService.sendOutOfStockAlert(productId, product.getName());
    }
    
    // Enviar actualización de stock
    StockUpdateMessage stockUpdate = new StockUpdateMessage(
        productId, product.getName(), oldStock, newStock
    );
    notificationService.sendStockUpdate(stockUpdate);
}
```
