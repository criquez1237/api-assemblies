package com.assembliestore.api.service.realtime.config;

import com.assembliestore.api.service.realtime.handler.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Endpoint para stock updates (solo MANAGEMENT)
        registry.addHandler(webSocketHandler, "/ws/stock")
                .setAllowedOrigins("*"); // En producción, especifica dominios permitidos

        // Endpoint para notificaciones generales (CLIENT, ADMIN, MANAGEMENT)
        registry.addHandler(webSocketHandler, "/ws/notifications")
                .setAllowedOrigins("*"); // En producción, especifica dominios permitidos

        // Endpoint general para cualquier tipo de comunicación
        registry.addHandler(webSocketHandler, "/ws/general")
                .setAllowedOrigins("*"); // En producción, especifica dominios permitidos
    }
}
