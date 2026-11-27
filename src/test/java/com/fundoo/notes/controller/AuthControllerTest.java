package com.fundoo.notes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoo.notes.dto.request.EmailRequestDto;
import com.fundoo.notes.dto.request.PasswordResetRequestDto;
import com.fundoo.notes.dto.request.VerifyEmailRequestDto;
import com.fundoo.notes.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .build();
    }

    @Test
    void resendsVerificationEmail() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("user@example.com");

        mockMvc.perform(post("/api/auth/verify-email/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        verify(authService).sendVerificationEmail("user@example.com");
    }

    @Test
    void verifiesEmail() throws Exception {
        VerifyEmailRequestDto request = new VerifyEmailRequestDto();
        request.setToken("verify-token");

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Email verified successfully."));

        verify(authService).verifyEmail("verify-token");
    }

    @Test
    void requestsPasswordReset() throws Exception {
        EmailRequestDto request = new EmailRequestDto();
        request.setEmail("user@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).requestPasswordReset("user@example.com");
    }

    @Test
    void resetsPassword() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken("reset-token");
        request.setNewPassword("NewPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).resetPassword("reset-token", "NewPassword123");
    }

    @Test
    void logsOutWithAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk());

        verify(authService).logout("Bearer jwt-token");
    }
}
