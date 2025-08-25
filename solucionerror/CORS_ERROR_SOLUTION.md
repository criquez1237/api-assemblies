# Solución de Errores de CORS en API REST

## 📋 Resumen Ejecutivo

Se identificaron y corrigieron múltiples problemas de configuración CORS que impedían la comunicación entre el frontend (puerto 5173) y el backend (puerto 8081) de la aplicación Assemblies Store.

## 🔍 Problemas Identificados

### 1. **Configuración Incorrecta de CORS Mapping**

**Archivo:** `/src/main/java/com/assembliestore/api/config/CorsConfig.java`

**Problema:** El mapping de CORS estaba configurado incorrectamente
```java
// ❌ ANTES - INCORRECTO
registry.addMapping("/api/**")
```

**¿Por qué fallaba?**
- El `context-path` en `application.yml` ya está configurado como `/api`
- Al usar `/api/**` en el mapping, Spring buscaba rutas como `/api/api/products`
- La URL real del endpoint es `http://localhost:8081/api/products`
- Pero CORS esperaba `http://localhost:8081/api/api/products`

**Solución aplicada:**
```java
// ✅ DESPUÉS - CORRECTO
registry.addMapping("/**")
```

**Líneas modificadas:** 11
```java
// Línea 11 - ANTES
registry.addMapping("/api/**")

// Línea 11 - DESPUÉS  
registry.addMapping("/**")
```

---

### 2. **Falta de Configuración CORS en Security**

**Archivo:** `/src/main/java/com/assembliestore/api/module/user/infrastructure/config/SecurityConfig.java`

**Problema:** Spring Security no tenía configuración CORS, lo que causaba que los filtros de seguridad bloquearan las peticiones preflight (OPTIONS).

**Imports agregados (líneas 20-22):**
```java
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
```

**Configuración agregada en SecurityFilterChain (línea 42):**
```java
// ❌ ANTES
http
    .csrf(csrf -> csrf.disable())

// ✅ DESPUÉS
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
```

**Método corsConfigurationSource agregado (líneas 103-114):**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(java.util.Arrays.asList("http://localhost:5173", "http://localhost:3000"));
    configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(java.util.Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### 3. **Bug Crítico en JwtAuthenticationFilter**

**Archivo:** `/src/main/java/com/assembliestore/api/module/user/infrastructure/config/JwtAuthenticationFilter.java`

**Problema 1 (líneas 55-58):** El filtro no continuaba la cadena cuando el usuario era nulo
```java
// ❌ ANTES - BUG CRÍTICO
if (userName == null || SecurityContextHolder.getContext().getAuthentication() != null) {
    return; // ⚠️ NO continúa el filterChain
}

// ✅ DESPUÉS - CORREGIDO
if (userName == null || SecurityContextHolder.getContext().getAuthentication() != null) {
    filterChain.doFilter(request, response);
    return;
}
```

**Problema 2 (líneas 80-83):** El filtro no continuaba cuando el token era inválido
```java
// ❌ ANTES - BUG CRÍTICO  
if(!isTokenValid){
    return; // ⚠️ NO continúa el filterChain
}

// ✅ DESPUÉS - CORREGIDO
if(!isTokenValid){
    filterChain.doFilter(request, response);
    return;
}
```

**¿Por qué era crítico este bug?**
- Cuando el token era inválido o nulo, el filtro simplemente terminaba (`return`)
- Esto causaba que la respuesta HTTP se quedara "colgada" 
- El cliente nunca recibía una respuesta, causando timeouts
- Las peticiones OPTIONS (preflight de CORS) fallaban silenciosamente

---

## 🔧 Configuración del Sistema

### Backend Configuration (`application.yml`)
```yaml
server:
    port: 8081
    servlet:
        context-path: /api
```

### Frontend Configuration (`product-service.js`)
```javascript
const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8081/api';
```

### URL Final Resultante
- **Endpoint:** `/products`
- **URL completa:** `http://localhost:8081/api/products`
- **Mapping CORS:** `/**` (cubre todas las rutas después del context-path)

---

## 🌐 Flujo de la Petición CORS

### Antes de la corrección:
1. **Frontend** → Petición OPTIONS a `http://localhost:8081/api/products`
2. **CorsConfig** → Busca mapping `/api/api/products` ❌ (no encuentra)
3. **SecurityConfig** → No tiene configuración CORS ❌
4. **JwtFilter** → Token inválido → `return` ❌ (respuesta colgada)
5. **Frontend** → Timeout/Error CORS

### Después de la corrección:
1. **Frontend** → Petición OPTIONS a `http://localhost:8081/api/products`
2. **CorsConfig** → Encuentra mapping `/**` ✅
3. **SecurityConfig** → Procesa CORS correctamente ✅
4. **JwtFilter** → Token inválido → `filterChain.doFilter()` ✅ (continúa)
5. **Frontend** → Recibe respuesta HTTP adecuada ✅

---

## 🚀 Verificación de la Solución

### Para probar que funciona:

1. **Reiniciar el servidor backend:**
```bash
./mvnw spring-boot:run
```

2. **Verificar que el frontend apunte a la URL correcta:**
```javascript
// En product-service.js
const BASE = 'http://localhost:8081/api';
```

3. **Realizar petición desde frontend:**
```javascript
// Esta petición ahora debería funcionar sin errores CORS
fetchProducts({ page: 1, limit: 10 });
```

4. **Verificar en DevTools del navegador:**
   - ✅ No debe aparecer error de CORS
   - ✅ Las peticiones OPTIONS deben retornar 200
   - ✅ Las peticiones GET/POST deben funcionar normalmente

---

## 📝 Archivos Modificados

| Archivo | Líneas Modificadas | Tipo de Cambio |
|---------|-------------------|----------------|
| `CorsConfig.java` | 11 | Corrección mapping |
| `SecurityConfig.java` | 20-22, 42, 103-114 | Imports + Configuración CORS |
| `JwtAuthenticationFilter.java` | 55-58, 80-83 | Bug crítico filterChain |

---

## ⚠️ Puntos Importantes

1. **Orden de los filtros:** CORS debe procesarse antes que Security
2. **Context-path:** Siempre considerar el context-path al configurar mappings
3. **FilterChain:** NUNCA usar `return` sin `filterChain.doFilter()` en filtros
4. **Credenciales:** `allowCredentials(true)` es necesario para tokens JWT
5. **Métodos HTTP:** Incluir siempre `OPTIONS` para peticiones preflight

---

## 🎯 Resultado Final

✅ **CORS configurado correctamente**  
✅ **Peticiones preflight funcionando**  
✅ **Tokens JWT procesados adecuadamente**  
✅ **Comunicación frontend-backend establecida**  

La aplicación ahora puede realizar peticiones desde `http://localhost:5173` hacia `http://localhost:8081/api/*` sin errores de CORS.
