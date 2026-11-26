package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;
import com.fundoo.notes.service.ReminderService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderControllerTest {

    @Mock
    private ReminderService reminderService;

    @InjectMocks
    private ReminderController reminderController;

    private LocalDateTime reminderTime;
    private ReminderResponseDto response;

    @BeforeEach
    void setUp() {
        reminderTime = LocalDateTime.now().plusDays(2).withNano(0);
        response = new ReminderResponseDto(100L, 20L, "Project", reminderTime);
    }

    @Test
    void createReminderDelegatesToServiceAndReturnsCreated() {
        ReminderRequestDto request = new ReminderRequestDto();
        request.setNoteId(20L);
        request.setReminderTime(reminderTime);
        when(reminderService.createReminder("Bearer token", request))
                .thenReturn(response);

        ResponseEntity<ReminderResponseDto> result =
                reminderController.createReminder(request, "Bearer token");

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(100L, result.getBody().getId());
    }

    @Test
    void getAllRemindersDelegatesToServiceAndReturnsOk() {
        when(reminderService.getAllReminders("Bearer token"))
                .thenReturn(List.of(response));

        ResponseEntity<List<ReminderResponseDto>> result =
                reminderController.getAllReminders("Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void updateReminderDelegatesToServiceAndReturnsOk() {
        ReminderUpdateRequestDto request = new ReminderUpdateRequestDto();
        request.setReminderTime(reminderTime);
        when(reminderService.updateReminder(100L, "Bearer token", request))
                .thenReturn(response);

        ResponseEntity<ReminderResponseDto> result =
                reminderController.updateReminder(100L, request, "Bearer token");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(100L, result.getBody().getId());
    }

    @Test
    void deleteReminderDelegatesToServiceAndReturnsNoContent() {
        ResponseEntity<Void> result =
                reminderController.deleteReminder(100L, "Bearer token");

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(reminderService).deleteReminder(100L, "Bearer token");
    }
}
