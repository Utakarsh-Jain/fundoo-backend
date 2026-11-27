package com.fundoo.notes.service;

import java.time.Duration;

public interface AuthTokenStore {

    void saveVerificationToken(String email, String token);

    String consumeVerificationToken(String token);

    void savePasswordResetToken(String email, String token);

    String consumePasswordResetToken(String token);

    void denyJwt(String token, Duration duration);

    boolean isJwtDenied(String token);
}
