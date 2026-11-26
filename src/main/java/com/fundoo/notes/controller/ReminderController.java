
package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;
import com.fundoo.notes.service.ReminderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Class Name  : ReminderController
 * Description : Exposes REST APIs for managing user reminders.
 * Author      : Utakarsh Jain
 * Date        : 26/11/2026
 */

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // Schedules a reminder for an authenticated user's note.
    @PostMapping
    public ResponseEntity<ReminderResponseDto> createReminder(
            @Valid @RequestBody ReminderRequestDto request,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                reminderService.createReminder(token, request)
        );
    }

    // Retrieves all reminders belonging to the authenticated user.
    @GetMapping
    public ResponseEntity<List<ReminderResponseDto>> getAllReminders(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                reminderService.getAllReminders(token)
        );
    }

    // Updates the time of an authenticated user's reminder.
    @PutMapping("/{reminderId}")
    public ResponseEntity<ReminderResponseDto> updateReminder(
            @PathVariable Long reminderId,
            @Valid @RequestBody ReminderUpdateRequestDto request,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                reminderService.updateReminder(reminderId, token, request)
        );
    }

    // Deletes an authenticated user's reminder.
    @DeleteMapping("/{reminderId}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable Long reminderId,
            @RequestHeader("Authorization") String token) {

        reminderService.deleteReminder(reminderId, token);

        return ResponseEntity.noContent().build();
    }
}
