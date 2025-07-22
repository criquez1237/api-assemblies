package com.assembliestore.api.service.realtime.handler;

import com.assembliestore.api.service.realtime.service.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;

@Component
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Autowired
    private WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Obtener la URI del WebSocket para determinar el tipo de conexión
        String uri = session.getUri().getPath();
        String connectionType = extractConnectionType(uri);
        
        logger.info("New WebSocket connection established: {} - Type: {}", session.getId(), connectionType);
        
        // Registrar la sesión en el manager
        sessionManager.addSession(session, connectionType);
        
        // Enviar mensaje de bienvenida
        String welcomeMessage = createWelcomeMessage(connectionType);
        session.sendMessage(new TextMessage(welcomeMessage));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            logger.info("Message received from session {}: {}", session.getId(), payload);
            
            try {
                // Procesar el mensaje recibido
                handleIncomingMessage(session, payload);
            } catch (Exception e) {
                logger.error("Error processing message from session {}: {}", session.getId(), e.getMessage());
                sendErrorMessage(session, "Error processing message: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error in session {}: {}", session.getId(), exception.getMessage());
        sessionManager.removeSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("WebSocket connection closed: {} - Status: {}", session.getId(), closeStatus);
        sessionManager.removeSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String extractConnectionType(String uri) {
        if (uri.contains("/ws/stock")) {
            return "STOCK";
        } else if (uri.contains("/ws/notifications")) {
            return "NOTIFICATIONS";
        } else if (uri.contains("/ws/general")) {
            return "GENERAL";
        }
        return "UNKNOWN";
    }

    private String createWelcomeMessage(String connectionType) {
        try {
            Map<String, Object> welcomeData = Map.of(
                "type", "WELCOME",
                "connectionType", connectionType,
                "message", "Connected successfully to " + connectionType + " channel",
                "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(welcomeData);
        } catch (Exception e) {
            logger.error("Error creating welcome message: {}", e.getMessage());
            return "{\"type\":\"WELCOME\",\"message\":\"Connected successfully\"}";
        }
    }

    private void handleIncomingMessage(WebSocketSession session, String payload) throws Exception {
        try {
            // Intentar parsear el mensaje como JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            
            String messageType = (String) messageData.get("type");
            
            switch (messageType) {
                case "PING":
                    handlePingMessage(session);
                    break;
                case "SUBSCRIBE":
                    handleSubscriptionMessage(session, messageData);
                    break;
                case "UNSUBSCRIBE":
                    handleUnsubscriptionMessage(session, messageData);
                    break;
                default:
                    logger.warn("Unknown message type: {}", messageType);
                    sendErrorMessage(session, "Unknown message type: " + messageType);
            }
            
        } catch (Exception e) {
            logger.error("Error parsing message: {}", e.getMessage());
            // Si no es JSON válido, tratarlo como mensaje de texto simple
            handleTextMessage(session, payload);
        }
    }

    private void handlePingMessage(WebSocketSession session) throws Exception {
        Map<String, Object> pongData = Map.of(
            "type", "PONG",
            "timestamp", System.currentTimeMillis()
        );
        String pongMessage = objectMapper.writeValueAsString(pongData);
        session.sendMessage(new TextMessage(pongMessage));
    }

    private void handleSubscriptionMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String channel = (String) messageData.get("channel");
        if (channel != null) {
            sessionManager.subscribeToChannel(session, channel);
            
            Map<String, Object> confirmData = Map.of(
                "type", "SUBSCRIPTION_CONFIRMED",
                "channel", channel,
                "message", "Subscribed to channel: " + channel
            );
            String confirmMessage = objectMapper.writeValueAsString(confirmData);
            session.sendMessage(new TextMessage(confirmMessage));
        }
    }

    private void handleUnsubscriptionMessage(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String channel = (String) messageData.get("channel");
        if (channel != null) {
            sessionManager.unsubscribeFromChannel(session, channel);
            
            Map<String, Object> confirmData = Map.of(
                "type", "UNSUBSCRIPTION_CONFIRMED",
                "channel", channel,
                "message", "Unsubscribed from channel: " + channel
            );
            String confirmMessage = objectMapper.writeValueAsString(confirmData);
            session.sendMessage(new TextMessage(confirmMessage));
        }
    }

    private void handleTextMessage(WebSocketSession session, String payload) throws Exception {
        // Manejar mensajes de texto simple
        logger.info("Text message from session {}: {}", session.getId(), payload);
        
        Map<String, Object> responseData = Map.of(
            "type", "TEXT_RESPONSE",
            "originalMessage", payload,
            "response", "Message received: " + payload
        );
        String responseMessage = objectMapper.writeValueAsString(responseData);
        session.sendMessage(new TextMessage(responseMessage));
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            Map<String, Object> errorData = Map.of(
                "type", "ERROR",
                "message", errorMessage,
                "timestamp", System.currentTimeMillis()
            );
            String errorJson = objectMapper.writeValueAsString(errorData);
            session.sendMessage(new TextMessage(errorJson));
        } catch (Exception e) {
            logger.error("Error sending error message to session {}: {}", session.getId(), e.getMessage());
        }
    }
}
