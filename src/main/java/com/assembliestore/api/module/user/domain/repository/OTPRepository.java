package com.assembliestore.api.module.user.domain.repository;

import com.assembliestore.api.module.user.domain.entities.OTPVerification;

import java.util.Optional;

public interface OTPRepository {
    

    OTPVerification save(OTPVerification otp);
    

    Optional<OTPVerification> findByEmailAndOtpCode(String email, String otpCode);
    

    Optional<OTPVerification> findActiveByEmail(String email);
    

    void deactivatePreviousOTPs(String email);
    
    OTPVerification markAsVerified(String id);
}
