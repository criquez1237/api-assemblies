# 🚀 Assemblies Store - Cliente WebSocket

Cliente Node.js para conectar y probar los endpoints WebSocket de Assemblies Store API.

## 📁 Estructura

```
websocket-client/
├── package.json           # Dependencias y scripts
├── client.js             # Cliente principal interactivo
├── stock-monitor.js      # Monitor de actualizaciones de stock
├── notification-client.js # Cliente de notificaciones
├── test-client.js        # Suite de pruebas automatizadas
└── README.md            # Esta documentación
```

## 🔧 Instalación

1. **Instalar dependencias:**
```bash
cd websocket-client
npm install
```

2. **Verificar que el servidor esté ejecutándose:**
   - La API REST debe estar corriendo en `http://localhost:8081`
   - Los endpoints WebSocket están en `ws://localhost:8081/api/ws/`

## 🎯 Endpoints WebSocket Disponibles

| Endpoint | Descripción | Autenticación |
|----------|-------------|---------------|
| `/api/ws/general` | Conexión general para todos los usuarios | No requerida |
| `/api/ws/notifications` | Notificaciones de usuario | Recomendada |
| `/api/ws/stock` | Actualizaciones de stock | **MANAGEMENT role requerido** |

## 🚀 Uso Rápido

### 1. Cliente Principal (Interactivo)
```bash
# Conectar al endpoint general
npm run client

# Conectar a notificaciones
npm run client notifications

# Conectar a stock (requiere auth)
npm run client stock
```

### 2. Monitor de Stock
```bash
# Sin autenticación (limitado)
npm run stock

# Con token de autorización
npm run stock -- YOUR_JWT_TOKEN

# Modo simulación para pruebas
npm run stock -- --simulate
```

### 3. Cliente de Notificaciones
```bash
# Básico
npm run notifications

# Con usuario y token
npm run notifications -- user123 YOUR_JWT_TOKEN
```

### 4. Suite de Pruebas
```bash
# Ejecutar todas las pruebas
npm test

# Prueba de carga con 20 conexiones
npm run test -- --load 20
```

## 💡 Comandos del Cliente Interactivo

Una vez conectado, puedes usar estos comandos:

| Comando | Descripción |
|---------|-------------|
| `ping` | Enviar ping al servidor |
| `subscribe <canal>` | Suscribirse a un canal específico |
| `unsubscribe <canal>` | Desuscribirse de un canal |
| `message <texto>` | Enviar mensaje de texto |
| `connect <endpoint>` | Conectar a otro endpoint |
| `stats` | Ver estadísticas |
| `help` | Mostrar ayuda |
| `exit` | Salir |

## 📨 Tipos de Mensajes WebSocket

### Mensajes que puedes enviar:

```javascript
// Ping
{ "type": "PING" }

// Suscribirse a canal
{ "type": "SUBSCRIBE", "channel": "user_notifications" }

// Desuscribirse
{ "type": "UNSUBSCRIBE", "channel": "user_notifications" }

// Mensaje de texto plano
"Hola servidor!"
```

### Mensajes que recibes:

```javascript
// Bienvenida
{ "type": "WELCOME", "message": "Conectado al WebSocket" }

// Pong
{ "type": "PONG" }

// Actualización de orden
{
  "type": "ORDER_STATUS_UPDATE",
  "title": "Estado de Orden Actualizado",
  "message": "Tu orden ha sido procesada",
  "data": {
    "orderId": "ORD123",
    "oldStatus": "PENDING",
    "newStatus": "PAID",
    "amount": 99.99
  }
}

// Alerta de stock
{
  "type": "OUT_OF_STOCK",
  "title": "Producto Agotado",
  "message": "El producto se ha agotado",
  "data": {
    "productId": "PROD123",
    "productName": "iPhone 15",
    "currentStock": 0
  }
}

// Actualización de stock
{
  "type": "STOCK_UPDATE",
  "data": {
    "productId": "PROD123",
    "productName": "iPhone 15",
    "previousStock": 10,
    "currentStock": 8,
    "changeType": "SALE"
  }
}
```

## 🔑 Autenticación

Para endpoints que requieren autenticación, incluye el header:
```
Authorization: Bearer YOUR_JWT_TOKEN
```

### Obtener Token JWT
1. Hacer login en la API REST:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@test.com", "password": "admin123"}'
```

2. Usar el token recibido en las conexiones WebSocket.

## 🧪 Pruebas Automatizadas

El `test-client.js` ejecuta las siguientes pruebas:

1. **Conexión General**: Verifica conectividad básica
2. **Endpoint Notificaciones**: Prueba suscripciones
3. **Endpoint Stock**: Verifica que requiere autenticación
4. **Manejo de Mensajes**: Prueba diferentes tipos de mensajes
5. **Reconexión**: Verifica capacidad de reconexión automática
6. **Prueba de Carga**: Múltiples conexiones simultáneas

### Resultados Esperados
```
✅ General Connection - 150ms
✅ Notifications Connection - 120ms  
✅ Stock Connection (no auth) - Correctly rejected
✅ Message Handling - 3 messages
✅ Reconnection - Successful

📊 RESUMEN:
   ✅ Exitosas: 5/5
   ❌ Fallidas: 0/5
   ⏱️  Tiempo total: 8.5s
   📈 Tasa de éxito: 100.0%
```

## 🔍 Solución de Problemas

### Error: ECONNREFUSED
```
❌ Error: connect ECONNREFUSED 127.0.0.1:8081
```
**Solución**: Verifica que el servidor Spring Boot esté ejecutándose.

### Error: Unauthorized
```
❌ Conexión cerrada: 1008 - Unauthorized
```
**Solución**: Proporciona un token JWT válido para el endpoint `/api/ws/stock`.

### Conexión se cierra inmediatamente
```
❌ Conexión cerrada: 1002 - Protocol error
```
**Solución**: Verifica que el formato de los mensajes JSON sea correcto.

### No recibo mensajes
1. Verifica que estés suscrito al canal correcto
2. Comprueba que el servidor esté enviando notificaciones
3. Revisa los logs del servidor Spring Boot

## 📚 Canales de Suscripción Disponibles

| Canal | Descripción | Endpoint |
|-------|-------------|----------|
| `user_notifications` | Notificaciones de usuario | notifications |
| `order_updates` | Actualizaciones de órdenes | notifications |
| `stock_updates` | Cambios de inventario | stock |
| `system_alerts` | Alertas del sistema | general |

## 🎨 Personalización

Para modificar los clientes:

1. **Cambiar URLs**: Edita las URLs en cada archivo cliente
2. **Nuevos Tipos de Mensaje**: Agrega handlers en `handleMessage()`
3. **Autenticación Personalizada**: Modifica los headers WebSocket
4. **Nuevos Comandos**: Añade casos en `processCommand()`

## 🔗 Enlaces Útiles

- [Documentación WebSocket Spring](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [Node.js ws library](https://github.com/websockets/ws)
- [JWT.io](https://jwt.io/) - Para decodificar tokens JWT

## 📝 Notas de Desarrollo

- Los clientes usan reconexión automática cuando es posible
- Los mensajes se formatean con colores para mejor legibilidad
- Todos los errores se capturan y muestran de forma amigable
- Los logs incluyen timestamps para debugging
- Compatible con Node.js 14+

---

🚀 **¡Listo para conectar con tu API WebSocket de Assemblies Store!** 🚀
