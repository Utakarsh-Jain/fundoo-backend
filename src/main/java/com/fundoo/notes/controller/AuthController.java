package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.EmailRequestDto;
import com.fundoo.notes.dto.request.PasswordResetRequestDto;
import com.fundoo.notes.dto.request.VerifyEmailRequestDto;
import com.fundoo.notes.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/verify-email/resend")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @Valid @RequestBody EmailRequestDto request) {

        authService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message",
                "If the account exists, a verification email has been sent."
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDto request) {

        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully."
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody EmailRequestDto request) {

        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message",
                "If the account exists, a password reset email has been sent."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody PasswordResetRequestDto request) {

        authService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );
        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully."
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader) {

        authService.logout(authorizationHeader);
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully."
        ));
    }
}
