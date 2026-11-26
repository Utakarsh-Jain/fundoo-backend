package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.DuplicateLabelException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LabelServiceImpl labelService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Test User", "user@example.com", "password");
        user.setId(1L);
    }

    private void mockAuthenticatedUser() {
        when(tokenService.isTokenValid("token")).thenReturn(true);
        when(tokenService.extractSubject("token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    private LabelRequestDto request(String name) {
        LabelRequestDto request = new LabelRequestDto();
        request.setName(name);
        return request;
    }

    @Test
    void updateLabelTrimsAndRenamesOwnLabel() {
        mockAuthenticatedUser();

        Label label = new Label("Work", user);
        label.setId(10L);

        when(labelRepository.findById(10L)).thenReturn(Optional.of(label));
        when(labelRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                eq("Office"), eq(1L), eq(10L))).thenReturn(false);
        when(labelRepository.save(label)).thenReturn(label);

        LabelResponseDto response = labelService.updateLabel(
                10L, "Bearer token", request("  Office  "));

        assertEquals(10L, response.getId());
        assertEquals("Office", response.getName());
        verify(labelRepository).save(label);
    }

    @Test
    void updateLabelAllowsKeepingTheSameName() {
        mockAuthenticatedUser();

        Label label = new Label("Work", user);
        label.setId(10L);

        when(labelRepository.findById(10L)).thenReturn(Optional.of(label));
        when(labelRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                "Work", 1L, 10L)).thenReturn(false);
        when(labelRepository.save(label)).thenReturn(label);

        LabelResponseDto response = labelService.updateLabel(
                10L, "Bearer token", request("Work"));

        assertEquals("Work", response.getName());
        verify(labelRepository).existsByNameIgnoreCaseAndUserIdAndIdNot(
                "Work", 1L, 10L);
    }

    @Test
    void updateLabelRejectsCaseInsensitiveDuplicate() {
        mockAuthenticatedUser();

        Label label = new Label("Work", user);
        label.setId(10L);

        when(labelRepository.findById(10L)).thenReturn(Optional.of(label));
        when(labelRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                "Personal", 1L, 10L)).thenReturn(true);

        assertThrows(
                DuplicateLabelException.class,
                () -> labelService.updateLabel(
                        10L, "Bearer token", request("Personal")));

        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void updateLabelRejectsMissingLabel() {
        mockAuthenticatedUser();
        when(labelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                LabelNotFoundException.class,
                () -> labelService.updateLabel(
                        99L, "Bearer token", request("Office")));

        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void updateLabelRejectsAnotherUsersLabel() {
        mockAuthenticatedUser();

        User otherUser = new User("Other", "other@example.com", "password");
        otherUser.setId(2L);
        Label label = new Label("Work", otherUser);
        label.setId(10L);

        when(labelRepository.findById(10L)).thenReturn(Optional.of(label));

        assertThrows(
                LabelNotFoundException.class,
                () -> labelService.updateLabel(
                        10L, "Bearer token", request("Office")));

        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    void updateLabelRejectsInvalidToken() {
        assertThrows(
                InvalidCredentialsException.class,
                () -> labelService.updateLabel(
                        10L, "", request("Office")));
    }

    @Test
    void createLabelRejectsCaseInsensitiveDuplicate() {
        mockAuthenticatedUser();

        Label existing = new Label("Work", user);
        when(labelRepository.findByNameIgnoreCaseAndUserId("Work", 1L))
                .thenReturn(Optional.of(existing));

        assertThrows(
                DuplicateLabelException.class,
                () -> labelService.createLabel(
                        "Bearer token", request("Work")));

        verify(labelRepository, never()).save(any(Label.class));
    }
}
