# 📋 Order API Tests - Bruno Collection

Esta carpeta contiene las pruebas de la API de órdenes usando Bruno REST Client.

## 🔗 Endpoints Disponibles

### **POST /api/orders** - Crear Orden
- **Auth**: Bearer Token requerido
- **Body**: JSON con productos, dirección de envío y método de pago

### **GET /api/orders/{id}** - Obtener Orden por ID
- **Auth**: Bearer Token requerido
- **Roles**: CLIENT, ADMIN, MANAGEMENT

### **GET /api/orders** - Obtener Todas las Órdenes
- **Auth**: Bearer Token requerido  
- **Roles**: ADMIN, MANAGEMENT únicamente

### **GET /api/orders/user/{userId}** - Órdenes por Usuario
- **Auth**: Bearer Token requerido
- **Roles**: CLIENT, ADMIN, MANAGEMENT

### **GET /api/orders/status/{status}** - Órdenes por Estado
- **Auth**: Bearer Token requerido
- **Roles**: ADMIN, MANAGEMENT únicamente

### **PATCH /api/orders/{id}/status** - Actualizar Estado
- **Auth**: Bearer Token requerido
- **Roles**: ADMIN, MANAGEMENT únicamente

### **DELETE /api/orders/{id}** - Eliminar Orden
- **Auth**: Bearer Token requerido
- **Roles**: ADMIN únicamente

---

## 📝 Formato JSON Correcto

### **✅ Crear Orden - Formato Correcto:**
```json
{
  "products": [
    {
      "productId": "string",
      "name": "string", 
      "unitPrice": number,
      "quantity": integer
    }
  ],
  "shippingAddress": {
    "street": "string",
    "city": "string",
    "country": "string", 
    "postalCode": "string"
  },
  "paymentMethod": "CREDIT_CARD" | "DEBIT_CARD" | "CASH"
}
```

### **❌ Formato Incorrecto (Causa Validation Error):**
```json
{
  "products": [
    {
      "nombre": "...",           ← INCORRECTO: debe ser "name"
      "precioUnitario": ...,     ← INCORRECTO: debe ser "unitPrice"
      "cantidad": ...            ← INCORRECTO: debe ser "quantity"
    }
  ],
  "shippingAddress": {
    "calle": "...",              ← INCORRECTO: debe ser "street"
    "ciudad": "...",             ← INCORRECTO: debe ser "city"
    "pais": "...",               ← INCORRECTO: debe ser "country"
    "codigoPostal": "..."        ← INCORRECTO: debe ser "postalCode"
  },
  "paymentMethod": "..."
}
```

---

## 🔐 Autenticación

**Token JWT Válido Incluido:**
```
eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJmNDA1Zjg4NC0yMjY3LTRiZjQtODFhYi0wYTRjMjU4YjYwZmYiLCJ1c2VyTmFtZSI6ImVranJpY25yaXUiLCJzdWIiOiJ0ZXN0MTRqQGV4YW1wbGUuY29tIiwiaWF0IjoxNzUzMTk5ODA5LCJleHAiOjE3NTMyODYyMDl9.MClsPbgHrjq0s0DncJdiUFhtUofjLal7bdQKDosInGLwsf66NFttXy0nheGaxEUlbr6kV3ZeGLQ0m2ZeEAqlrw
```

**Información del Token:**
- Usuario: `ekjricnriu`
- Email: `test14j@example.com`
- JTI: `f405f884-2267-4bf4-81ab-0a4c258b60ff`
- Expira: 2025-07-22

---

## 📊 Estados de Órdenes

| Estado | Descripción | Transiciones Permitidas |
|--------|-------------|------------------------|
| **PROCESSING** | Orden recién creada | → CONFIRMED, CANCELLED |
| **CONFIRMED** | Orden confirmada | → PREPARING, CANCELLED |
| **PREPARING** | Preparando envío | → SHIPPED, CANCELLED |
| **SHIPPED** | Enviada | → DELIVERED |
| **DELIVERED** | Entregada | → REFUNDED |
| **CANCELLED** | Cancelada | ❌ Estado final |
| **REFUNDED** | Reembolsada | ❌ Estado final |

---

## 🧪 Archivos de Prueba

### **Casos de Éxito:**
- `Create Order.bru` - Crear orden con tarjeta de crédito
- `Create Order - Cash Payment.bru` - Crear orden con efectivo
- `Get Order by ID.bru` - Obtener orden específica
- `Get Orders by User ID.bru` - Órdenes de usuario
- `Update Order Status - Confirm.bru` - Confirmar orden

### **Casos de Error/Validación:**
- `Create Order - Missing Products.bru` - Error: lista de productos vacía
- `Create Order - Missing Shipping Address.bru` - Error: dirección requerida
- `Create Order - Unauthorized.bru` - Error: sin token
- `Update Order Status - Invalid Transition.bru` - Error: transición inválida
- `Delete Order - Cannot Delete Shipped.bru` - Error: no se puede eliminar

### **Casos de Autorización:**
- `Get All Orders - Admin.bru` - Solo ADMIN/MANAGEMENT
- `Delete Order.bru` - Solo ADMIN

---

## 🚀 Mejoras Implementadas

### **✅ Seguridad Mejorada:**
- **UserId extraído automáticamente del token JWT** - No se envía en el body
- **UUID generado automáticamente** - IDs únicos y seguros
- **Validación de tokens** - Autenticación requerida

### **✅ Validaciones:**
- Lista de productos no vacía
- Dirección de envío requerida  
- Método de pago requerido
- Transiciones de estado controladas
- Permisos por rol

### **✅ Arquitectura Limpia:**
- Separación de responsabilidades
- DTOs bien estructurados
- Manejo de errores consistente

---

## 📝 Notas Importantes

1. **Campo userId eliminado** - Ahora se extrae del token JWT automáticamente
2. **Nombres en inglés** - Los DTOs usan nombres en inglés (`shippingAddress`, `paymentMethod`)
3. **Token incluido** - Todas las pruebas incluyen el token JWT válido
4. **Validaciones activas** - Spring Validation habilitado
5. **Roles específicos** - Algunos endpoints requieren roles específicos

---

*Documentación actualizada: 22 de julio de 2025*
*Pruebas creadas para API Orders con mejoras de seguridad implementadas*
