package com.assembliestore.api.service.email.dto;

public class EmailResponse {
    private boolean success;
    private String messageId;
    private String error;

    public EmailResponse() {}

    public EmailResponse(boolean success, String messageId, String error) {
        this.success = success;
        this.messageId = messageId;
        this.error = error;
    }

    public static EmailResponse success(String messageId) {
        return new EmailResponse(true, messageId, null);
    }

    public static EmailResponse error(String error) {
        return new EmailResponse(false, null, error);
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
