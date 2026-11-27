package com.fundoo.notes.service.impl;

import com.fundoo.notes.service.AuthTokenStore;
import com.fundoo.notes.service.RedisService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisAuthTokenStore implements AuthTokenStore {

    private static final String VERIFICATION_PREFIX = "AUTH:VERIFY:";
    private static final String RESET_PREFIX = "AUTH:RESET:";
    private static final String DENYLIST_PREFIX = "AUTH:DENY:";

    private final RedisService redisService;
    private final Duration verificationTokenDuration;
    private final Duration passwordResetTokenDuration;

    public RedisAuthTokenStore(
            RedisService redisService,
            @Value("${app.redis.verification-token-expiration}") long verificationSeconds,
            @Value("${app.redis.password-reset-token-expiration}") long resetSeconds) {

        this.redisService = redisService;
        this.verificationTokenDuration = Duration.ofSeconds(verificationSeconds);
        this.passwordResetTokenDuration = Duration.ofSeconds(resetSeconds);
    }

    @Override
    public void saveVerificationToken(String email, String token) {
        redisService.save(
                VERIFICATION_PREFIX + token,
                email,
                verificationTokenDuration
        );
    }

    @Override
    public String consumeVerificationToken(String token) {
        return consume(VERIFICATION_PREFIX + token);
    }

    @Override
    public void savePasswordResetToken(String email, String token) {
        redisService.save(
                RESET_PREFIX + token,
                email,
                passwordResetTokenDuration
        );
    }

    @Override
    public String consumePasswordResetToken(String token) {
        return consume(RESET_PREFIX + token);
    }

    @Override
    public void denyJwt(String token, Duration duration) {
        redisService.save(DENYLIST_PREFIX + token, "revoked", duration);
    }

    @Override
    public boolean isJwtDenied(String token) {
        return redisService.exists(DENYLIST_PREFIX + token);
    }

    private String consume(String key) {
        Object value = redisService.get(key);
        if (value == null) {
            return null;
        }

        redisService.delete(key);
        return String.valueOf(value);
    }
}
