package com.assembliestore.api.module.user.infrastructure.adapter.dto;

public class SignUpResponse {

    private String message;
    private boolean success;
    private String email;

    public SignUpResponse() {}

    public SignUpResponse(String message, boolean success, String email) {
        this.message = message;
        this.success = success;
        this.email = email;
    }

    public static SignUpResponse success(String email) {
        return new SignUpResponse(
            "Se ha enviado un código de verificación a tu correo electrónico. Por favor, revisa tu bandeja de entrada.",
            true,
            email
        );
    }

    public static SignUpResponse error(String message) {
        return new SignUpResponse(message, false, null);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
