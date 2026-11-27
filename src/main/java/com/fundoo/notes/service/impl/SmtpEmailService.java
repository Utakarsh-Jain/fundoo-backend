package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String username;
    private final String frontendBaseUrl;

    public SmtpEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String username,
            @Value("${app.frontend.base-url}") String frontendBaseUrl) {

        this.mailSender = mailSender;
        this.username = username;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void sendVerificationEmail(String email, String token) {
        String link = frontendBaseUrl + "/verify-email?token=" + token;
        send(
                email,
                "Verify your Fundoo Notes email",
                "Welcome to Fundoo Notes!\n\n"
                        + "Verify your email address using this link:\n"
                        + link
                        + "\n\nThis link expires in 24 hours."
        );
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String link = frontendBaseUrl + "/reset-password?token=" + token;
        send(
                email,
                "Reset your Fundoo Notes password",
                "We received a password reset request for your account.\n\n"
                        + "Reset your password using this link:\n"
                        + link
                        + "\n\nThis link expires in 30 minutes. "
                        + "Ignore this email if you did not request it."
        );
    }

    private void send(String to, String subject, String text) {
        if (username == null || username.isBlank()) {
            logger.warn("Auth email skipped because MAIL_USERNAME is not configured");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        logger.info("Auth email sent to {}", to);
    }
}
