package com.fundoo.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private SmtpEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new SmtpEmailService(
                mailSender,
                "noreply@example.com",
                "http://localhost:5173"
        );
    }

    @Test
    void sendsVerificationLink() {
        emailService.sendVerificationEmail("user@example.com", "verify-token");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getSubject().contains("Verify"));
        assertTrue(message.getText().contains(
                "http://localhost:5173/verify-email?token=verify-token"
        ));
    }

    @Test
    void sendsPasswordResetLink() {
        emailService.sendPasswordResetEmail("user@example.com", "reset-token");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getSubject().contains("Reset"));
        assertTrue(message.getText().contains(
                "http://localhost:5173/reset-password?token=reset-token"
        ));
    }

    @Test
    void skipsSendingWhenMailUsernameIsBlank() {
        SmtpEmailService service = new SmtpEmailService(
                mailSender,
                "",
                "http://localhost:5173"
        );

        service.sendVerificationEmail("user@example.com", "token");

        verify(mailSender, never()).send(
                org.mockito.ArgumentMatchers.any(SimpleMailMessage.class)
        );
    }
}
