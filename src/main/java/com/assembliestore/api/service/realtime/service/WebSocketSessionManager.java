package com.assembliestore.api.service.realtime.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);

    // Mapa para guardar las sesiones activas por tipo de conexión
    private final Map<String, Set<WebSocketSession>> sessionsByType = new ConcurrentHashMap<>();
    
    // Mapa para guardar información de cada sesión
    private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    
    // Mapa para suscripciones a canales específicos
    private final Map<String, Set<WebSocketSession>> channelSubscriptions = new ConcurrentHashMap<>();

    /**
     * Agregar una nueva sesión WebSocket
     */
    public void addSession(WebSocketSession session, String connectionType) {
        String sessionId = session.getId();
        
        // Agregar a la lista por tipo
        sessionsByType.computeIfAbsent(connectionType, k -> ConcurrentHashMap.newKeySet()).add(session);
        
        // Guardar información de la sesión
        SessionInfo sessionInfo = new SessionInfo(sessionId, connectionType, System.currentTimeMillis());
        sessionInfoMap.put(sessionId, sessionInfo);
        
        logger.info("Session added: {} - Type: {} - Total sessions: {}", 
                   sessionId, connectionType, getTotalActiveSessions());
    }

    /**
     * Remover una sesión WebSocket
     */
    public void removeSession(WebSocketSession session) {
        String sessionId = session.getId();
        SessionInfo sessionInfo = sessionInfoMap.remove(sessionId);
        
        if (sessionInfo != null) {
            // Remover de la lista por tipo
            Set<WebSocketSession> sessions = sessionsByType.get(sessionInfo.getConnectionType());
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    sessionsByType.remove(sessionInfo.getConnectionType());
                }
            }
            
            // Remover de todas las suscripciones de canales
            channelSubscriptions.values().forEach(subs -> subs.remove(session));
            
            logger.info("Session removed: {} - Type: {} - Duration: {}ms - Total sessions: {}", 
                       sessionId, sessionInfo.getConnectionType(), 
                       System.currentTimeMillis() - sessionInfo.getConnectedAt(),
                       getTotalActiveSessions());
        }
    }

    /**
     * Suscribir una sesión a un canal específico
     */
    public void subscribeToChannel(WebSocketSession session, String channel) {
        channelSubscriptions.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(session);
        logger.info("Session {} subscribed to channel: {}", session.getId(), channel);
    }

    /**
     * Desuscribir una sesión de un canal específico
     */
    public void unsubscribeFromChannel(WebSocketSession session, String channel) {
        Set<WebSocketSession> subscribers = channelSubscriptions.get(channel);
        if (subscribers != null) {
            subscribers.remove(session);
            if (subscribers.isEmpty()) {
                channelSubscriptions.remove(channel);
            }
        }
        logger.info("Session {} unsubscribed from channel: {}", session.getId(), channel);
    }

    /**
     * Enviar mensaje a todas las sesiones de un tipo específico
     */
    public void broadcastToType(String connectionType, String message) {
        Set<WebSocketSession> sessions = sessionsByType.get(connectionType);
        if (sessions != null && !sessions.isEmpty()) {
            broadcastToSessions(sessions, message);
            logger.info("Message broadcasted to {} sessions of type: {}", sessions.size(), connectionType);
        }
    }

    /**
     * Enviar mensaje a todas las sesiones suscritas a un canal
     */
    public void broadcastToChannel(String channel, String message) {
        Set<WebSocketSession> subscribers = channelSubscriptions.get(channel);
        if (subscribers != null && !subscribers.isEmpty()) {
            broadcastToSessions(subscribers, message);
            logger.info("Message broadcasted to {} subscribers of channel: {}", subscribers.size(), channel);
        }
    }

    /**
     * Enviar mensaje a una sesión específica
     */
    public boolean sendToSession(String sessionId, String message) {
        for (Set<WebSocketSession> sessions : sessionsByType.values()) {
            for (WebSocketSession session : sessions) {
                if (session.getId().equals(sessionId)) {
                    try {
                        session.sendMessage(new TextMessage(message));
                        return true;
                    } catch (Exception e) {
                        logger.error("Error sending message to session {}: {}", sessionId, e.getMessage());
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Obtener estadísticas de conexiones activas
     */
    public Map<String, Object> getConnectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSessions", getTotalActiveSessions());
        stats.put("sessionsByType", getSessionCountByType());
        stats.put("activeChannels", channelSubscriptions.keySet());
        stats.put("channelSubscribers", getChannelSubscriberCounts());
        return stats;
    }

    /**
     * Obtener total de sesiones activas
     */
    public int getTotalActiveSessions() {
        return sessionInfoMap.size();
    }

    /**
     * Obtener conteo de sesiones por tipo
     */
    public Map<String, Integer> getSessionCountByType() {
        Map<String, Integer> counts = new HashMap<>();
        sessionsByType.forEach((type, sessions) -> counts.put(type, sessions.size()));
        return counts;
    }

    /**
     * Obtener conteo de suscriptores por canal
     */
    public Map<String, Integer> getChannelSubscriberCounts() {
        Map<String, Integer> counts = new HashMap<>();
        channelSubscriptions.forEach((channel, subscribers) -> counts.put(channel, subscribers.size()));
        return counts;
    }

    /**
     * Método helper para enviar mensaje a un conjunto de sesiones
     */
    private void broadcastToSessions(Set<WebSocketSession> sessions, String message) {
        Set<WebSocketSession> toRemove = new HashSet<>();
        
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                } else {
                    toRemove.add(session);
                }
            } catch (Exception e) {
                logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
                toRemove.add(session);
            }
        }
        
        // Limpiar sesiones cerradas
        toRemove.forEach(this::removeSession);
    }

    /**
     * Clase interna para guardar información de la sesión
     */
    private static class SessionInfo {
        private final String sessionId;
        private final String connectionType;
        private final long connectedAt;

        public SessionInfo(String sessionId, String connectionType, long connectedAt) {
            this.sessionId = sessionId;
            this.connectionType = connectionType;
            this.connectedAt = connectedAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getConnectionType() {
            return connectionType;
        }

        public long getConnectedAt() {
            return connectedAt;
        }
    }
}
