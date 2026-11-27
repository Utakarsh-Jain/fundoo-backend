package com.fundoo.notes.service.impl;

import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.InvalidTokenException;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.AuthTokenStore;
import com.fundoo.notes.service.EmailService;
import com.fundoo.notes.service.PasswordService;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthTokenStore authTokenStore;

    @Mock
    private EmailService emailService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordService,
                tokenService,
                authTokenStore,
                emailService,
                86400000L
        );
    }

    @Test
    void sendsVerificationEmailForUnverifiedUser() {
        User user = new User("Utakarsh", "user@example.com", "hash");
        user.setId(1L);
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        authService.sendVerificationEmail("user@example.com");

        verify(authTokenStore).saveVerificationToken(
                eq("user@example.com"),
                anyString()
        );
        verify(emailService).sendVerificationEmail(
                eq("user@example.com"),
                anyString()
        );
    }

    @Test
    void skipsVerificationEmailForVerifiedUser() {
        User user = new User("Utakarsh", "user@example.com", "hash");
        user.setEmailVerified(true);
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        authService.sendVerificationEmail("user@example.com");

        verify(authTokenStore, never()).saveVerificationToken(
                anyString(),
                anyString()
        );
        verify(emailService, never()).sendVerificationEmail(
                anyString(),
                anyString()
        );
    }

    @Test
    void verifiesEmailUsingStoredToken() {
        User user = new User("Utakarsh", "user@example.com", "hash");
        when(authTokenStore.consumeVerificationToken("verify-token"))
                .thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        authService.verifyEmail("verify-token");

        assertTrue(user.isEmailVerified());
        verify(userRepository).save(user);
    }

    @Test
    void rejectsInvalidVerificationToken() {
        when(authTokenStore.consumeVerificationToken("bad-token"))
                .thenReturn(null);

        assertThrows(
                InvalidTokenException.class,
                () -> authService.verifyEmail("bad-token")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void sendsPasswordResetEmailForKnownUser() {
        User user = new User("Utakarsh", "user@example.com", "hash");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        authService.requestPasswordReset("user@example.com");

        verify(authTokenStore).savePasswordResetToken(
                eq("user@example.com"),
                anyString()
        );
        verify(emailService).sendPasswordResetEmail(
                eq("user@example.com"),
                anyString()
        );
    }

    @Test
    void resetsPasswordUsingStoredToken() {
        User user = new User("Utakarsh", "user@example.com", "old-hash");
        when(authTokenStore.consumePasswordResetToken("reset-token"))
                .thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordService.encodePassword("NewPassword123"))
                .thenReturn("new-hash");

        authService.resetPassword("reset-token", "NewPassword123");

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void invalidatesJwtDuringLogout() {
        when(tokenService.isTokenValid("jwt-token")).thenReturn(true);

        authService.logout("Bearer jwt-token");

        verify(authTokenStore).denyJwt(
                "jwt-token",
                Duration.ofMillis(86400000L)
        );
    }

    @Test
    void skipsInvalidationForMissingBearerToken() {
        authService.logout(null);

        verify(authTokenStore, never()).denyJwt(
                anyString(),
                any(Duration.class)
        );
    }
}
