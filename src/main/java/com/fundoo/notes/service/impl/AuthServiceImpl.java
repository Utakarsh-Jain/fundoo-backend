package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidTokenException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.AuthService;
import com.fundoo.notes.service.AuthTokenStore;
import com.fundoo.notes.service.EmailService;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;
    private final AuthTokenStore authTokenStore;
    private final EmailService emailService;
    private final Duration tokenValidity;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordService passwordService,
            TokenService tokenService,
            AuthTokenStore authTokenStore,
            EmailService emailService,
            @Value("${app.jwt.expiration-ms}") long tokenValidityMillis) {

        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
        this.authTokenStore = authTokenStore;
        this.emailService = emailService;
        this.tokenValidity = Duration.ofMillis(tokenValidityMillis);
    }

    @Override
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.isEmailVerified()) {
            return;
        }

        String token = UUID.randomUUID().toString();
        authTokenStore.saveVerificationToken(email, token);
        emailService.sendVerificationEmail(email, token);
        logger.info("Verification email prepared for user ID: {}", user.getId());
    }

    @Override
    public void verifyEmail(String token) {
        String email = authTokenStore.consumeVerificationToken(token);
        if (email == null) {
            throw new InvalidTokenException(
                    "Invalid or expired verification token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException(
                        "Invalid or expired verification token"));

        user.setEmailVerified(true);
        userRepository.save(user);
        logger.info("Email verified for user ID: {}", user.getId());
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        authTokenStore.savePasswordResetToken(email, token);
        emailService.sendPasswordResetEmail(email, token);
        logger.info("Password reset email prepared for user ID: {}", user.getId());
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String email = authTokenStore.consumePasswordResetToken(token);
        if (email == null) {
            throw new InvalidTokenException(
                    "Invalid or expired password reset token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException(
                        "Invalid or expired password reset token"));

        user.setPassword(passwordService.encodePassword(newPassword));
        userRepository.save(user);
        logger.info("Password reset for user ID: {}", user.getId());
    }

    @Override
    public void logout(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null || !tokenService.isTokenValid(token)) {
            return;
        }

        authTokenStore.denyJwt(token, tokenValidity);
        logger.info("Token invalidated during logout");
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
