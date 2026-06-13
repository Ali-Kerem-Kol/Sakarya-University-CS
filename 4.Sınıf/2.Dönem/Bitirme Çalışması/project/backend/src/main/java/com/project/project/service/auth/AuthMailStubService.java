package com.project.project.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Sends auth-related emails via SMTP.
 */
@Service
public class AuthMailStubService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthMailStubService.class);
    private static final String DEFAULT_PUBLIC_BASE_URL = "http://localhost:3000";

    private final JavaMailSender mailSender;
    private final String from;
    private final String publicBaseUrl;

    public AuthMailStubService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:noreply@32bit.com.tr}") String from,
            @Value("${app.public-base-url:http://localhost:3000}") String publicBaseUrl
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.publicBaseUrl = sanitizeBaseUrl(publicBaseUrl);
    }

    public void sendVerificationMail(String email, String token) {
        String verifyLink = publicBaseUrl + "/verify?token=" + token;
        String textBody = """
                Merhaba,

                E-posta adresinizi doğrulamak için aşağıdaki bağlantıya tıklayın:
                %s

                (Opsiyonel) Doğrulama tokenı: %s
                """.formatted(verifyLink, token);
        String htmlBody = """
                <p>Merhaba,</p>
                <p>E-posta adresinizi doğrulamak için aşağıdaki bağlantıya tıklayın:</p>
                <p><a href="%s">%s</a></p>
                <p><strong>Opsiyonel token:</strong> %s</p>
                """.formatted(verifyLink, verifyLink, token);
        sendMimeMail(email, "Verify your email", textBody, htmlBody);
        LOGGER.info("Email verification sent: email={}, token={}", email, token);
    }

    public void sendPasswordResetMail(String email, String token) {
        String resetLink = publicBaseUrl + "/reset-password?token=" + token;
        String textBody = """
                Merhaba,

                Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:
                %s

                (Opsiyonel) Şifre sıfırlama tokenı: %s
                """.formatted(resetLink, token);
        String htmlBody = """
                <p>Merhaba,</p>
                <p>Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:</p>
                <p><a href="%s">%s</a></p>
                <p><strong>Opsiyonel token:</strong> %s</p>
                """.formatted(resetLink, resetLink, token);
        sendMimeMail(email, "Password reset", textBody, htmlBody);
        LOGGER.info("Password reset email sent: email={}, token={}", email, token);
    }

    private void sendMimeMail(String to, String subject, String textBody, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);
            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send auth mail", ex);
        }
    }

    private String sanitizeBaseUrl(String raw) {
        String candidate = StringUtils.hasText(raw) ? raw.trim() : DEFAULT_PUBLIC_BASE_URL;
        while (candidate.endsWith("/")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }
        return candidate;
    }
}
