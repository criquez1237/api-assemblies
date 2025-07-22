# 🔐 Cambios en la API de Órdenes - Seguridad Mejorada

## ✅ Implementación Completada

### **Cambios Realizados:**

#### **1. Seguridad Mejorada**
- ✅ **UserId extraído del token JWT** - Elimina posibilidad de crear órdenes ajenas
- ✅ **Validación de token** - Solo usuarios autenticados pueden crear órdenes
- ✅ **Generación de UUID** - IDs únicos y consistentes con otros módulos

#### **2. Archivos Modificados:**
- `CreateOrderRequestDto.java` - Removido campo `userId` 
- `OrderMapper.java` - Agregado método que recibe `userId` como parámetro
- `OrderController.java` - Extrae `userId` del token JWT
- `OrderService.java` - Genera UUID para las órdenes
- `OrderFirestoreRepository.java` - Valida que el ID venga desde el servicio

---

## 📋 Formato de Request ANTES vs DESPUÉS

### **❌ ANTES (Inseguro):**
```json
POST /orders
Authorization: Bearer <token>

{
  "userId": "user123",  ← 🚨 PROBLEMA: Usuario podía crear órdenes ajenas
  "products": [
    {
      "productId": "prod1",
      "nombre": "Producto 1", 
      "precioUnitario": 100,
      "cantidad": 2
    }
  ],
  "direccionEnvio": {
    "calle": "123 Main St",
    "ciudad": "Ciudad",
    "pais": "País",
    "codigoPostal": "12345"
  },
  "metodoPago": "CREDIT_CARD"
}
```

### **✅ DESPUÉS (Seguro):**
```json
POST /orders  
Authorization: Bearer <token>

{
  "products": [
    {
      "productId": "prod1",
      "nombre": "Producto 1",
      "precioUnitario": 100, 
      "cantidad": 2
    }
  ],
  "direccionEnvio": {
    "calle": "123 Main St",
    "ciudad": "Ciudad", 
    "pais": "País",
    "codigoPostal": "12345"
  },
  "metodoPago": "CREDIT_CARD"
}
```

---

## 🔄 Flujo de Procesamiento

### **Nuevo Flujo Seguro:**

1. **Cliente** envía request sin `userId`
2. **OrderController** extrae `userId` del token JWT
3. **OrderMapper** usa el `userId` extraído del token
4. **OrderService** genera UUID único para la orden
5. **OrderFirestoreRepository** valida y guarda la orden

### **Beneficios de Seguridad:**

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Autenticación** | Token opcional en lógica | Token requerido y validado |
| **Autorización** | Usuario podía crear órdenes ajenas | Solo puede crear sus propias órdenes |
| **Integridad** | ID podía ser manipulado | UUID generado automáticamente |
| **Consistencia** | Firestore ID | UUID estándar |

---

## 🛡️ Validaciones de Seguridad Implementadas

### **1. Validación de Token:**
```java
private String getUserIdFromToken(HttpServletRequest request) {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new RuntimeException("Invalid authorization header");
    }
    
    final String jwtToken = authHeader.substring(7);
    var jwtTokenDto = tokenPort.findByToken(jwtToken);
    
    if (jwtTokenDto == null) {
        throw new RuntimeException("Invalid token");
    }
    
    return jwtTokenDto.getUserId(); // ← UserId extraído del token
}
```

### **2. Generación Segura de ID:**
```java
// Generate UUID if not present
if (order.getId() == null || order.getId().isEmpty()) {
    order.setId(UUID.randomUUID().toString());
}
```

### **3. Validación en Repositorio:**
```java
// Validate that order has an ID (should be generated in service layer)
if (order.getId() == null || order.getId().isEmpty()) {
    throw new IllegalArgumentException("Order ID must be provided");
}
```

---

## 🧪 Testing

### **Casos de Prueba Recomendados:**

1. **✅ Token válido** - Debe crear orden exitosamente
2. **❌ Sin token** - Debe retornar 401 Unauthorized  
3. **❌ Token inválido** - Debe retornar 403 Forbidden
4. **❌ Token expirado** - Debe retornar 401 Unauthorized

### **Ejemplo de Response Exitosa:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "order": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userId": "user123",  ← Extraído del token
      "products": [...],
      "total": 200.00,
      "status": "PROCESSING",
      "orderDate": "2025-07-22T10:30:00Z"
    },
    "clientSecret": "pi_xxx_secret_xxx"  ← Para pagos con tarjeta
  }
}
```

---

## 📝 Migración para Clientes

### **Frontend Changes Required:**

```javascript
// ❌ ANTES
const createOrder = async (orderData) => {
  const payload = {
    userId: getCurrentUserId(), // ← Remover esta línea
    products: orderData.products,
    direccionEnvio: orderData.direccionEnvio,
    metodoPago: orderData.metodoPago
  };
  
  return await fetch('/orders', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });
};

// ✅ DESPUÉS  
const createOrder = async (orderData) => {
  const payload = {
    // userId removido - se extrae del token automáticamente
    products: orderData.products,
    direccionEnvio: orderData.direccionEnvio, 
    metodoPago: orderData.metodoPago
  };
  
  return await fetch('/orders', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`, // ← Token requerido
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });
};
```

---

## ⚡ Compatibilidad

- **Versión API:** No hay cambio de versión, pero el comportamiento cambió
- **Breaking Change:** ✅ SÍ - Clientes deben remover `userId` del payload
- **Rollback:** Posible revertir cambios si es necesario
- **Testing:** Se recomienda probar exhaustivamente antes de desplegar

---

## 🎯 Próximos Pasos Recomendados

1. **Actualizar documentación de API** (Swagger)
2. **Notificar a equipos frontend** sobre los cambios
3. **Crear tests de integración** 
4. **Considerar versionado de API** para futuras migraciones
5. **Implementar logging** para monitorear el uso

---

*Implementación completada el 22 de julio de 2025*
*Mejoras de seguridad aplicadas a módulo Sale/Orders*
