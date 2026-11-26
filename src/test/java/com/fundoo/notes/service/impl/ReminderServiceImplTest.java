package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.Reminder;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.event.ReminderEvent;
import com.fundoo.notes.exception.DuplicateReminderException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.exception.ReminderNotFoundException;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.ReminderRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.ReminderEventPublisher;
import com.fundoo.notes.service.TokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private ReminderEventPublisher eventPublisher;

    @InjectMocks
    private ReminderServiceImpl reminderService;

    private User user;
    private Note note;
    private LocalDateTime reminderTime;

    @BeforeEach
    void setUp() {
        user = new User("Test User", "user@example.com", "password");
        user.setId(1L);
        note = new Note("Project", "Description", user);
        note.setId(20L);
        reminderTime = LocalDateTime.now().plusDays(2).withNano(0);
    }

    private void mockAuthenticatedUser() {
        when(tokenService.isTokenValid("token")).thenReturn(true);
        when(tokenService.extractSubject("token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    private ReminderRequestDto createRequest() {
        ReminderRequestDto request = new ReminderRequestDto();
        request.setNoteId(note.getId());
        request.setReminderTime(reminderTime);
        return request;
    }

    private ReminderUpdateRequestDto updateRequest(LocalDateTime time) {
        ReminderUpdateRequestDto request = new ReminderUpdateRequestDto();
        request.setReminderTime(time);
        return request;
    }

    private Reminder existingReminder() {
        Reminder reminder = new Reminder(note, user, reminderTime);
        reminder.setId(100L);
        return reminder;
    }

    @Test
    void createReminderPersistsAndPublishesEvent() {
        mockAuthenticatedUser();
        when(noteRepository.findByIdAndUserId(20L, 1L))
                .thenReturn(Optional.of(note));
        when(reminderRepository.findByNoteIdAndUserId(20L, 1L))
                .thenReturn(Optional.empty());
        when(reminderRepository.save(any(Reminder.class)))
                .thenAnswer(invocation -> {
                    Reminder saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        ReminderResponseDto response = reminderService.createReminder(
                "Bearer token", createRequest());

        assertEquals(100L, response.getId());
        assertEquals(20L, response.getNoteId());
        assertEquals("Project", response.getNoteTitle());
        assertEquals(reminderTime, response.getReminderTime());
        verify(eventPublisher).publish(any(ReminderEvent.class));
    }

    @Test
    void createReminderRejectsDuplicateForSameNote() {
        mockAuthenticatedUser();
        when(noteRepository.findByIdAndUserId(20L, 1L))
                .thenReturn(Optional.of(note));
        when(reminderRepository.findByNoteIdAndUserId(20L, 1L))
                .thenReturn(Optional.of(existingReminder()));

        assertThrows(
                DuplicateReminderException.class,
                () -> reminderService.createReminder("Bearer token", createRequest())
        );
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void createReminderRejectsNoteOwnedByAnotherUser() {
        mockAuthenticatedUser();
        when(noteRepository.findByIdAndUserId(20L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoteNotFoundException.class,
                () -> reminderService.createReminder("Bearer token", createRequest())
        );
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void getAllRemindersMapsUserReminders() {
        mockAuthenticatedUser();
        when(reminderRepository.findByUserIdOrderByReminderTimeAsc(1L))
                .thenReturn(List.of(existingReminder()));

        List<ReminderResponseDto> result =
                reminderService.getAllReminders("Bearer token");

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
        assertEquals("Project", result.get(0).getNoteTitle());
        assertEquals(reminderTime, result.get(0).getReminderTime());
    }

    @Test
    void updateReminderChangesTimeAndPublishesEvent() {
        mockAuthenticatedUser();
        Reminder reminder = existingReminder();
        when(reminderRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(reminder));
        when(reminderRepository.save(reminder)).thenReturn(reminder);
        LocalDateTime newTime = reminderTime.plusDays(1);

        ReminderResponseDto response = reminderService.updateReminder(
                100L, "Bearer token", updateRequest(newTime));

        assertEquals(100L, response.getId());
        assertEquals(newTime, response.getReminderTime());
        verify(eventPublisher).publish(any(ReminderEvent.class));
    }

    @Test
    void updateReminderRejectsReminderOwnedByAnotherUser() {
        mockAuthenticatedUser();
        when(reminderRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReminderNotFoundException.class,
                () -> reminderService.updateReminder(
                        100L, "Bearer token", updateRequest(reminderTime))
        );
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void deleteReminderRemovesOwnReminder() {
        mockAuthenticatedUser();
        Reminder reminder = existingReminder();
        when(reminderRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(reminder));

        reminderService.deleteReminder(100L, "Bearer token");

        verify(reminderRepository).delete(reminder);
    }

    @Test
    void deleteReminderRejectsMissingReminder() {
        mockAuthenticatedUser();
        when(reminderRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ReminderNotFoundException.class,
                () -> reminderService.deleteReminder(100L, "Bearer token")
        );
        verify(reminderRepository, never()).delete(any(Reminder.class));
    }

    @Test
    void invalidTokenIsRejected() {
        when(tokenService.isTokenValid("bad-token")).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> reminderService.getAllReminders("Bearer bad-token")
        );
    }
}
