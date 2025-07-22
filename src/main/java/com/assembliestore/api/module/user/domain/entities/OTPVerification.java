package com.assembliestore.api.module.user.domain.entities;

import java.time.LocalDateTime;

public class OTPVerification {
    
    private String id;
    private String userId;
    private String email;
    private String otpCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean verified;
    private boolean active;

    public OTPVerification() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(10); // OTP válido por 10 minutos
        this.verified = false;
        this.active = true;
    }

    public OTPVerification(String userId, String email, String otpCode) {
        this();
        this.userId = userId;
        this.email = email;
        this.otpCode = otpCode;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
