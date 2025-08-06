package com.assembliestore.api.service.email;

import com.assembliestore.api.config.ResendConfig;
import com.assembliestore.api.service.email.dto.EmailRequest;
import com.assembliestore.api.service.email.dto.EmailResponse;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final ResendConfig resendConfig;

    public EmailService(ResendConfig resendConfig) {
        this.resendConfig = resendConfig;
    }

    private Resend resend;

    private Resend getResendClient() {
        if (resend == null) {
            String apiKey = resendConfig.getApiKey();
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("Resend API key not configured. Emails will not be sent.");
                return null;
            }
            resend = new Resend(apiKey);
        }
        return resend;
    }

    public EmailResponse sendEmail(EmailRequest emailRequest) {
        try {
            Resend client = getResendClient();
            if (client == null) {
                logger.warn("Email not sent - Resend client not configured");
                return EmailResponse.error("Email service not configured");
            }

            // Cargar y procesar la plantilla HTML
            String htmlContent = loadTemplate(emailRequest.getTemplateName(), emailRequest.getVariables());

            // IMPORTANTE: En modo prueba de Resend, solo podemos enviar a hr323413@resend.dev
            String testEmail = "hr323413@resend.dev";
            
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Assemblies Store <" + resendConfig.getFromEmail() + ">")
                    .to(testEmail) // Siempre usar el email de prueba
                    .subject(emailRequest.getSubject() + " [Para: " + emailRequest.getTo() + "]") // Agregar destinatario real al subject
                    .html(htmlContent)
                    .build();

            CreateEmailResponse data = client.emails().send(params);
            
            logger.info("Email sent successfully to: {} (intended for: {}) - Message ID: {}", 
                       testEmail, emailRequest.getTo(), data.getId());
            return EmailResponse.success(data.getId());

        } catch (ResendException e) {
            logger.error("Failed to send email to: {} - Error: {}", emailRequest.getTo(), e.getMessage());
            return EmailResponse.error("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending email to: {} - Error: {}", emailRequest.getTo(), e.getMessage());
            return EmailResponse.error("Unexpected error: " + e.getMessage());
        }
    }

    private String loadTemplate(String templateName, Map<String, Object> variables) {
        try {
            Path templatePath = Paths.get("src/main/resources/templates/email/" + templateName + ".html");
            String template = Files.readString(templatePath);
            
            // Reemplazar variables en la plantilla
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    template = template.replace("{{" + entry.getKey() + "}}", 
                        entry.getValue() != null ? entry.getValue().toString() : "");
                }
            }
            
            return template;
        } catch (IOException e) {
            logger.error("Failed to load email template: {} - Error: {}", templateName, e.getMessage());
            return "<html><body><h1>Error loading email template</h1></body></html>";
        }
    }

    // Métodos específicos para cada tipo de email
    
    /**
     * Enviar email de bienvenida al registrarse
     */
    public EmailResponse sendWelcomeEmail(String userEmail, String userName) {
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "userEmail", userEmail,
            "companyName", "Assemblies Store",
            "supportEmail", "support@assembliesstore.com"
        );

        EmailRequest request = new EmailRequest(
            "hr323413@resend.dev",
            "¡Bienvenido a Assemblies Store!",
            "welcome",
            variables
        );

        return sendEmail(request);
    }

    /**
     * Enviar email de cancelación de orden
     */
    public EmailResponse sendOrderCancellationEmail(String userEmail, String userName, String orderId, String refundMethod) {
        Map<String, Object> variables = Map.of(
            "userName", userEmail,
            "orderId", orderId,
            "refundMethod", refundMethod,
            "companyName", "Assemblies Store",
            "supportEmail", "support@assembliesstore.com"
        );

        EmailRequest request = new EmailRequest(
            "hr323413@resend.dev",
            "Orden Cancelada - " + orderId,
            "order-cancellation",
            variables
        );

        return sendEmail(request);
    }

    /**
     * Enviar código OTP (preparado para implementación futura)
     */
    public EmailResponse sendOTPEmail(String userEmail, String userName, String otpCode) {
        Map<String, Object> variables = Map.of(
            "userName", userName,
            "otpCode", otpCode,
            "expirationMinutes", "10",
            "companyName", "Assemblies Store"
        );

        EmailRequest request = new EmailRequest(
            "hr323413@resend.dev",
            "Tu código de verificación - Assemblies Store",
            "otp-verification",
            variables
        );

        return sendEmail(request);
    }
}
