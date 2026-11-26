
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
import com.fundoo.notes.service.ReminderService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Class Name  : ReminderServiceImpl
 * Description : Implements reminder scheduling and management using JWT.
 * Author      : Utakarsh Jain
 * Date        : 26/11/2026
 */

@Service
public class ReminderServiceImpl implements ReminderService {

    private static final Logger logger =
            LoggerFactory.getLogger(ReminderServiceImpl.class);

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ReminderEventPublisher eventPublisher;

    public ReminderServiceImpl(
            ReminderRepository reminderRepository,
            NoteRepository noteRepository,
            UserRepository userRepository,
            TokenService tokenService,
            ReminderEventPublisher eventPublisher) {

        this.reminderRepository = reminderRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ReminderResponseDto createReminder(
            String token,
            ReminderRequestDto request) {

        User user = findUserFromToken(token);

        Note note = noteRepository.findByIdAndUserId(
                request.getNoteId(),
                user.getId()
        ).orElseThrow(() -> new NoteNotFoundException(
                "Note not found"));

        if (reminderRepository.findByNoteIdAndUserId(
                note.getId(), user.getId()).isPresent()) {

            throw new DuplicateReminderException(
                    "Reminder already exists for this note");
        }

        Reminder reminder = reminderRepository.save(
                new Reminder(note, user, request.getReminderTime()));

        publishEvent(reminder, user);

        logger.info(
                "Reminder created successfully. Reminder ID: {}, User ID: {}",
                reminder.getId(),
                user.getId()
        );

        return mapToResponse(reminder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponseDto> getAllReminders(String token) {

        User user = findUserFromToken(token);

        return reminderRepository
                .findByUserIdOrderByReminderTimeAsc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReminderResponseDto updateReminder(
            Long reminderId,
            String token,
            ReminderUpdateRequestDto request) {

        User user = findUserFromToken(token);

        Reminder reminder = findReminderForUser(reminderId, user);
        reminder.setReminderTime(request.getReminderTime());

        Reminder updatedReminder = reminderRepository.save(reminder);

        publishEvent(updatedReminder, user);

        logger.info(
                "Reminder updated successfully. Reminder ID: {}, User ID: {}",
                reminderId,
                user.getId()
        );

        return mapToResponse(updatedReminder);
    }

    @Override
    @Transactional
    public void deleteReminder(Long reminderId, String token) {

        User user = findUserFromToken(token);
        Reminder reminder = findReminderForUser(reminderId, user);

        reminderRepository.delete(reminder);

        logger.info(
                "Reminder deleted successfully. Reminder ID: {}, User ID: {}",
                reminderId,
                user.getId()
        );
    }

    // Finds a reminder while verifying that it belongs to the user.
    private Reminder findReminderForUser(Long reminderId, User user) {

        return reminderRepository.findByIdAndUserId(reminderId, user.getId())
                .orElseThrow(() -> new ReminderNotFoundException(
                        "Reminder not found"));
    }

    // Publishes the reminder details to the configured RabbitMQ exchange.
    private void publishEvent(Reminder reminder, User user) {

        eventPublisher.publish(new ReminderEvent(
                user.getId(),
                reminder.getNote().getId(),
                user.getEmail(),
                reminder.getNote().getTitle(),
                reminder.getReminderTime()
        ));
    }

    // Validates the JWT and retrieves the authenticated user.
    private User findUserFromToken(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException(
                    "Authorization token is required");
        }

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7).trim()
                : token.trim();

        if (!tokenService.isTokenValid(jwt)) {
            throw new InvalidCredentialsException(
                    "Invalid or expired token");
        }

        String email = tokenService.extractSubject(jwt);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "User associated with token not found"));
    }

    // Converts a Reminder entity into its response DTO.
    private ReminderResponseDto mapToResponse(Reminder reminder) {

        return new ReminderResponseDto(
                reminder.getId(),
                reminder.getNote().getId(),
                reminder.getNote().getTitle(),
                reminder.getReminderTime()
        );
    }
}
