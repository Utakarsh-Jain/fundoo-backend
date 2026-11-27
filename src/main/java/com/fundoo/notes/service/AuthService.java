package com.fundoo.notes.service;

public interface AuthService {

    void sendVerificationEmail(String email);

    void verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void logout(String authorizationHeader);
}
