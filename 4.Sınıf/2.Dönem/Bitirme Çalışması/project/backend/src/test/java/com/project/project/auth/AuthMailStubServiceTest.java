package com.project.project.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import com.project.project.service.auth.AuthMailStubService;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class AuthMailStubServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void verificationMailContainsClickableVerifyLink() throws Exception {
        AuthMailStubService service = new AuthMailStubService(
                javaMailSender,
                "noreply@32bit.com.tr",
                "http://localhost:3000"
        );

        service.sendVerificationMail("student@ogr.sakarya.edu.tr", "verify-token-123");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());
        String raw = toRaw(captor.getValue());
        assertThat(raw).contains("/verify?token");
        assertThat(raw).contains("verify-token-123");
        assertThat(raw).contains("http://localhost:3000/verify");
    }

    @Test
    void resetMailContainsClickableResetLinkWithFallback() throws Exception {
        AuthMailStubService service = new AuthMailStubService(
                javaMailSender,
                "noreply@32bit.com.tr",
                ""
        );

        service.sendPasswordResetMail("student@ogr.sakarya.edu.tr", "reset-token-456");

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(captor.capture());
        String raw = toRaw(captor.getValue());
        assertThat(raw).contains("/reset-password?token");
        assertThat(raw).contains("reset-token-456");
        assertThat(raw).contains("http://localhost:3000/reset-password");
    }

    private String toRaw(MimeMessage message) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        message.writeTo(out);
        return out.toString(StandardCharsets.UTF_8);
    }
}
