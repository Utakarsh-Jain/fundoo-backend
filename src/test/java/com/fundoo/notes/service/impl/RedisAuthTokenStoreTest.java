package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.RedisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAuthTokenStoreTest {

    @Mock
    private RedisService redisService;

    private RedisAuthTokenStore store;

    @BeforeEach
    void setUp() {
        store = new RedisAuthTokenStore(redisService, 86400L, 1800L);
    }

    @Test
    void savesAndConsumesVerificationToken() {
        when(redisService.get("AUTH:VERIFY:token"))
                .thenReturn("user@example.com");

        store.saveVerificationToken("user@example.com", "token");
        String email = store.consumeVerificationToken("token");

        assertEquals("user@example.com", email);
        verify(redisService).save(
                "AUTH:VERIFY:token",
                "user@example.com",
                Duration.ofSeconds(86400)
        );
        verify(redisService).delete("AUTH:VERIFY:token");
    }

    @Test
    void returnsNullForMissingResetToken() {
        String email = store.consumePasswordResetToken("missing");

        assertEquals(null, email);
        verify(redisService, never()).delete("AUTH:RESET:missing");
    }

    @Test
    void savesPasswordResetTokenWithShortExpiry() {
        store.savePasswordResetToken("user@example.com", "reset-token");

        verify(redisService).save(
                "AUTH:RESET:reset-token",
                "user@example.com",
                Duration.ofSeconds(1800)
        );
    }

    @Test
    void deniesAndChecksJwt() {
        when(redisService.exists("AUTH:DENY:jwt")).thenReturn(true);

        store.denyJwt("jwt", Duration.ofSeconds(60));
        boolean denied = store.isJwtDenied("jwt");

        assertTrue(denied);
        verify(redisService).save(
                "AUTH:DENY:jwt",
                "revoked",
                Duration.ofSeconds(60)
        );

        when(redisService.exists("AUTH:DENY:other")).thenReturn(false);
        assertFalse(store.isJwtDenied("other"));
    }
}
