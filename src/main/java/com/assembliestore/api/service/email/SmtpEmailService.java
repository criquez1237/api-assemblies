package com.assembliestore.api.service.email;

import com.assembliestore.api.config.SmtpConfig;
import com.assembliestore.api.service.email.dto.EmailRequest;
import com.assembliestore.api.service.email.dto.EmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class SmtpEmailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final SmtpConfig smtpConfig;

    public SmtpEmailService(JavaMailSender mailSender, SmtpConfig smtpConfig) {
        this.mailSender = mailSender;
        this.smtpConfig = smtpConfig;
    }

    public EmailResponse sendEmail(EmailRequest emailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String html = loadTemplate(emailRequest.getTemplateName(), emailRequest.getVariables());

            helper.setText(html, true);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setFrom(smtpConfig.getFrom());

            mailSender.send(message);

            logger.info("SMTP email sent to {} via host {}", emailRequest.getTo(), smtpConfig.getHost());
            return EmailResponse.success("smtp-sent");

        } catch (MailException e) {
            logger.error("SMTP failed to send email to {} - {}", emailRequest.getTo(), e.getMessage());
            return EmailResponse.error("SMTP send failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error sending SMTP email to {} - {}", emailRequest.getTo(), e.getMessage());
            return EmailResponse.error("Unexpected error: " + e.getMessage());
        }
    }

    private String loadTemplate(String templateName, Map<String, Object> variables) {
        try {
            Path templatePath = Paths.get("src/main/resources/templates/email/" + templateName + ".html");
            String template = Files.readString(templatePath);
            if (variables != null) {
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    template = template.replace("{{" + entry.getKey() + "}}",
                            entry.getValue() != null ? entry.getValue().toString() : "");
                }
            }
            return template;
        } catch (Exception e) {
            logger.error("Failed to load email template: {} - {}", templateName, e.getMessage());
            return "<html><body><h1>Error loading email template</h1></body></html>";
        }
    }

    public EmailResponse sendWelcomeEmail(String userEmail, String userName) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "userEmail", userEmail,
                "companyName", "Assemblies Store",
                "supportEmail", "support@assembliesstore.com"
        );

        EmailRequest request = new EmailRequest(
                userEmail,
                "¡Bienvenido a Assemblies Store!",
                "welcome",
                variables
        );

        return sendEmail(request);
    }

    public EmailResponse sendOTPEmail(String userEmail, String userName, String otpCode) {
        Map<String, Object> variables = Map.of(
                "userName", userName,
                "otpCode", otpCode,
                "expirationMinutes", "10",
                "companyName", "Assemblies Store"
        );

        EmailRequest request = new EmailRequest(
                userEmail,
                "Tu código de verificación - Assemblies Store",
                "otp-verification",
                variables
        );

        return sendEmail(request);
    }
}
