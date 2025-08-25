package com.assembliestore.api.module.user.application.services;

import com.assembliestore.api.module.user.domain.entities.OTPVerification;
import com.assembliestore.api.module.user.domain.repository.OTPRepository;
import com.assembliestore.api.service.email.SmtpEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
public class OTPService {

    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private OTPRepository otpRepository;

    @Autowired
    private SmtpEmailService emailService;

    /**
     * Generar y enviar código OTP
     */
    public OTPVerification generateAndSendOTP(String userId, String email, String userName) {
        // Desactivar OTPs previos
        otpRepository.deactivatePreviousOTPs(email);

        // Generar nuevo código OTP de 6 dígitos
        String otpCode = generateOTPCode();

        // Crear nueva verificación OTP
        OTPVerification otp = new OTPVerification(userId, email, otpCode);
        otp.setId(UUID.randomUUID().toString());

        // Guardar en la base de datos
        OTPVerification savedOTP = otpRepository.save(otp);

        // Enviar email con el código OTP
        try {
            emailService.sendOTPEmail(email, userName, otpCode);
            logger.info("OTP enviado exitosamente a: {}", email);
        } catch (Exception e) {
            logger.error("Error al enviar OTP a {}: {}", email, e.getMessage());
            // No fallar la creación del OTP si el email falla
        }

        return savedOTP;
    }

    /**
     * Verificar código OTP
     */
    public boolean verifyOTP(String email, String otpCode) {
        Optional<OTPVerification> otpOpt = otpRepository.findByEmailAndOtpCode(email, otpCode);
        
        if (!otpOpt.isPresent()) {
            logger.warn("OTP no encontrado para email: {} y código: {}", email, otpCode);
            return false;
        }

        OTPVerification otp = otpOpt.get();

        // Verificar si el OTP está activo
        if (!otp.isActive()) {
            logger.warn("OTP inactivo para email: {}", email);
            return false;
        }

        // Verificar si el OTP no ha expirado
        if (otp.isExpired()) {
            logger.warn("OTP expirado para email: {}", email);
            return false;
        }

        // Verificar si ya fue usado
        if (otp.isVerified()) {
            logger.warn("OTP ya fue verificado para email: {}", email);
            return false;
        }

        // Marcar como verificado
        otpRepository.markAsVerified(otp.getId());
        
        logger.info("OTP verificado exitosamente para email: {}", email);
        return true;
    }

    /**
     * Generar código OTP de 6 dígitos
     */
    private String generateOTPCode() {
        int code = secureRandom.nextInt(900000) + 100000; // Genera número entre 100000 y 999999
        return String.valueOf(code);
    }
}
