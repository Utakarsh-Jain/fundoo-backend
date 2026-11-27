
package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.AuthTokenStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/*
 * Class Name  : JwtTokenServiceTest
 * Description : Tests JWT generation, subject extraction,
 *               signature validation and token expiration.
 * Author      : Utakarsh Jain
 * Date        : 14/11/2026
 */
class JwtTokenServiceTest {

    private JwtTokenService tokenService;
    private AuthTokenStore authTokenStore;

    // Test-only secret. Never use this key in the real application.
    private final String testSecret =
            Base64.getEncoder().encodeToString(
                    "0123456789ABCDEF0123456789ABCDEF".getBytes()
            );

    @BeforeEach
    void setUp() {
        authTokenStore = mock(AuthTokenStore.class);
        tokenService = new JwtTokenService(testSecret, 60000, authTokenStore);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        String token =
                tokenService.generateToken("user@example.com");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractSubjectFromToken() {

        String token =
                tokenService.generateToken("user@example.com");

        String subject = tokenService.extractSubject(token);

        assertEquals("user@example.com", subject);
    }

    @Test
    void shouldValidateCorrectToken() {

        String token =
                tokenService.generateToken("user@example.com");

        assertTrue(tokenService.isTokenValid(token));
    }

    @Test
    void shouldRejectInvalidToken() {

        assertFalse(tokenService.isTokenValid("invalid.token.value"));
    }

    @Test
    void shouldRejectExpiredToken() {

        JwtTokenService expiredTokenService =
                new JwtTokenService(testSecret, -1000, authTokenStore);

        String expiredToken =
                expiredTokenService.generateToken("user@example.com");

        assertFalse(expiredTokenService.isTokenValid(expiredToken));
    }

    @Test
    void shouldRejectDeniedToken() {
        String token = tokenService.generateToken("user@example.com");
        when(authTokenStore.isJwtDenied(token)).thenReturn(true);

        assertFalse(tokenService.isTokenValid(token));
    }
}
