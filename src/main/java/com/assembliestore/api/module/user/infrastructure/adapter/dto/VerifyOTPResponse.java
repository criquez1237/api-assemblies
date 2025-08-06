package com.assembliestore.api.module.user.infrastructure.adapter.dto;

public class VerifyOTPResponse {

    private String message;
    private boolean success;
    private TokenResponse tokens;

    public VerifyOTPResponse() {}

    public VerifyOTPResponse(String message, boolean success, TokenResponse tokens) {
        this.message = message;
        this.success = success;
        this.tokens = tokens;
    }

    public static VerifyOTPResponse success(TokenResponse tokens) {
        return new VerifyOTPResponse(
            "Cuenta activada exitosamente. ¡Bienvenido!",
            true,
            tokens
        );
    }

    public static VerifyOTPResponse error(String message) {
        return new VerifyOTPResponse(message, false, null);
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

    public TokenResponse getTokens() {
        return tokens;
    }

    public void setTokens(TokenResponse tokens) {
        this.tokens = tokens;
    }
}
