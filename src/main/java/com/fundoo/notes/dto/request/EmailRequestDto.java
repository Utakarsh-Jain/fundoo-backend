package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EmailRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    public EmailRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
