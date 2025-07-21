# Configuración de Swagger con Autenticación JWT

## Resumen
La API de Assemblies Store está configurada con Swagger/OpenAPI 3 para documentación automática y requiere autenticación JWT Bearer Token para la mayoría de endpoints.

## Acceso a Swagger UI
- **URL**: `http://localhost:8081/api/swagger-ui.html`
- **Documentación API**: `http://localhost:8081/api/v3/api-docs`

## Cómo Usar la Autenticación en Swagger

### 1. Obtener Token JWT
Primero necesitas obtener un token de autenticación usando uno de estos endpoints:
- `POST /api/auth/signin` - Para usuarios existentes
- `POST /api/auth/signup` - Para nuevos usuarios

### 2. Configurar el Token en Swagger UI
1. Ve a Swagger UI: `http://localhost:8081/api/swagger-ui.html`
2. Busca el botón **"Authorize"** (🔒) en la parte superior derecha
3. Haz clic en **"Authorize"**
4. En el campo **"bearerAuth"**, ingresa tu token JWT con el formato:
   ```
   Bearer tu_token_jwt_aqui
   ```
   Por ejemplo:
   ```
   Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjQwOTk1MjAwLCJleHAiOjE2NDA5OTg4MDB9.abc123def456ghi789
   ```
5. Haz clic en **"Authorize"**
6. Haz clic en **"Close"**

### 3. Probar Endpoints Autenticados
Ahora puedes probar cualquier endpoint que requiera autenticación. El token se enviará automáticamente en el header `Authorization` de cada request.

## Endpoints Configurados

### Sin Autenticación (🔓)
- `POST /auth/signin` - Iniciar sesión
- `POST /auth/signup` - Registrarse

### Con Autenticación Requerida (🔒)
- `POST /categorie/save` - Crear categoría
- `PUT /categorie/update` - Actualizar categoría
- `DELETE /categorie/delete/{categoryId}` - Eliminar categoría
- `GET /categorie/find/{categoryId}` - Buscar categoría por ID
- `GET /categorie/find-all` - Obtener todas las categorías
- `PATCH /categorie/toggle-active/{categoryId}` - Cambiar estado activo
- `PATCH /categorie/toggle-visible/{categoryId}` - Cambiar visibilidad

## Configuración Técnica

### SwaggerConfig.java
```java
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
```

### En los Controllers
```java
// Para requerir autenticación en toda la clase
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    // Todos los métodos requieren autenticación
}

// Para endpoints sin autenticación
@Tag(name = "Authentication", description = "Endpoints de autenticación")
public class AuthController {
    // Estos endpoints NO requieren autenticación
}
```

## Troubleshooting

### Error 401 "Unauthorized"
- Verifica que el token esté configurado correctamente en Swagger
- Asegúrate de incluir la palabra "Bearer " antes del token
- Verifica que el token no haya expirado

### Error 403 "Forbidden"
- Verifica que el usuario tiene permisos para acceder al recurso
- Confirma que el token es válido y pertenece a un usuario activo

### Token Expirado
- Vuelve a hacer login usando `/auth/signin`
- Configura el nuevo token en Swagger UI

## Configuración de application.yml
```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    try-it-out-enabled: true
    filter: true
```
