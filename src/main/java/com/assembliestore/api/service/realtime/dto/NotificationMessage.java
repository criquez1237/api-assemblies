package com.assembliestore.api.service.realtime.dto;

import java.time.LocalDateTime;

public class NotificationMessage {
    
    private String type;           // STOCK_UPDATE, ORDER_STATUS, GENERAL_NOTIFICATION
    private String title;
    private String message;
    private Object data;           // Datos específicos del mensaje
    private String targetRole;     // CLIENT, ADMIN, MANAGEMENT, ALL
    private String targetChannel;  // stock, notifications, general
    private LocalDateTime timestamp;
    private String priority;       // LOW, MEDIUM, HIGH, URGENT

    public NotificationMessage() {
        this.timestamp = LocalDateTime.now();
        this.priority = "MEDIUM";
    }

    public NotificationMessage(String type, String title, String message) {
        this();
        this.type = type;
        this.title = title;
        this.message = message;
    }

    public NotificationMessage(String type, String title, String message, Object data, String targetRole) {
        this(type, title, message);
        this.data = data;
        this.targetRole = targetRole;
    }

    // Getters y Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(String targetChannel) {
        this.targetChannel = targetChannel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", targetRole='" + targetRole + '\'' +
                ", targetChannel='" + targetChannel + '\'' +
                ", priority='" + priority + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
