package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyEmailRequestDto {

    @NotBlank(message = "Verification token is required")
    private String token;

    public VerifyEmailRequestDto() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
