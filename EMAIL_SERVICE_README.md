# Servicio de Email con Resend

## Configuración

### 1. Configurar las propiedades en `application.yml`

```yaml
resend:
  api-key: tu_api_key_de_resend_aqui  # Obtenla desde tu panel de Resend
  from-email: noreply@tu-dominio.com  # Email verificado en Resend
```

### 2. Obtener tu API Key de Resend

1. Ve a [resend.com](https://resend.com) y crea una cuenta
2. Ve a tu panel de control
3. Navega a "API Keys" 
4. Crea una nueva API key
5. Copia y pega la API key en tu `application.yml`

### 3. Verificar tu dominio

1. En tu panel de Resend, ve a "Domains"
2. Agrega tu dominio
3. Configura los registros DNS según las instrucciones
4. Una vez verificado, puedes usar emails de ese dominio

## Plantillas de Email Disponibles

### 1. Welcome Email (`welcome.html`)
- **Propósito**: Email de bienvenida cuando un usuario se registra
- **Variables**: `{{userName}}`
- **Uso**: Llamar a `/email/send-welcome`

### 2. Order Cancellation (`order-cancellation.html`)
- **Propósito**: Notificar cancelación de orden y reembolso
- **Variables**: `{{userName}}`, `{{orderId}}`
- **Uso**: Llamar a `/email/send-order-cancellation`

### 3. OTP Verification (`otp-verification.html`)
- **Propósito**: Enviar código de verificación OTP
- **Variables**: `{{userName}}`, `{{otpCode}}`
- **Uso**: Llamar a `/email/send-otp`

## API Endpoints

### Enviar Email de Bienvenida
```bash
POST /api/email/send-welcome
Content-Type: application/json

{
    "email": "usuario@ejemplo.com",
    "userName": "Juan Pérez"
}
```

### Enviar Email de Cancelación de Orden
```bash
POST /api/email/send-order-cancellation
Content-Type: application/json

{
    "email": "usuario@ejemplo.com",
    "userName": "Juan Pérez",
    "orderId": "ORD-12345"
}
```

### Enviar Email de Verificación OTP
```bash
POST /api/email/send-otp
Content-Type: application/json

{
    "email": "usuario@ejemplo.com",
    "userName": "Juan Pérez",
    "otpCode": "123456"
}
```

## Integración con Otros Módulos

### En el Registro de Usuario

```java
@Autowired
private EmailService emailService;

public void registerUser(UserRegistrationRequest request) {
    // ... lógica de registro ...
    
    // Enviar email de bienvenida
    Map<String, Object> variables = new HashMap<>();
    variables.put("userName", user.getName());
    
    EmailRequest emailRequest = new EmailRequest();
    emailRequest.setTo(user.getEmail());
    emailRequest.setSubject("¡Bienvenido a Assemblies Store!");
    emailRequest.setTemplateName("welcome");
    emailRequest.setVariables(variables);
    
    emailService.sendEmail(emailRequest);
}
```

### En el Webhook de Pagos (Cancelación)

```java
// En PaymentController.java - cuando el pago falla
if ("failed".equals(status)) {
    // ... restaurar stock ...
    
    // Enviar email de cancelación
    Map<String, Object> variables = new HashMap<>();
    variables.put("userName", order.getUserName());
    variables.put("orderId", order.getId());
    
    EmailRequest emailRequest = new EmailRequest();
    emailRequest.setTo(order.getUserEmail());
    emailRequest.setSubject("Cancelación de Orden - " + order.getId());
    emailRequest.setTemplateName("order-cancellation");
    emailRequest.setVariables(variables);
    
    emailService.sendEmail(emailRequest);
}
```

## Estructura de Archivos

```
src/main/java/com/assembliestore/api/
├── config/
│   └── ResendConfig.java           # Configuración de Resend
├── service/email/
│   ├── EmailService.java           # Servicio principal
│   └── dto/
│       ├── EmailRequest.java       # DTO para requests
│       └── EmailResponse.java      # DTO para responses
└── module/email/application/controllers/
    └── EmailController.java        # Controlador REST

src/main/resources/templates/email/
├── welcome.html                    # Plantilla de bienvenida
├── order-cancellation.html        # Plantilla de cancelación
└── otp-verification.html           # Plantilla de OTP
```

## Notas de Desarrollo

- Las plantillas usan sintaxis `{{variable}}` para sustitución de variables
- El servicio maneja automáticamente la carga de plantillas desde `src/main/resources/templates/email/`
- Los emails fallarán silenciosamente si la API key no está configurada (se mostrará un warning en logs)
- Todas las plantillas están diseñadas con CSS inline para mejor compatibilidad con clientes de email

## Próximos Pasos

1. Configurar tu API key de Resend en `application.yml`
2. Verificar tu dominio en el panel de Resend
3. Integrar las llamadas de email en los flujos de registro y cancelación de órdenes
4. Implementar la lógica de OTP si es necesaria
5. Hacer pruebas con emails reales
