
package com.fundoo.notes.service;

import com.fundoo.notes.dto.request.ReminderRequestDto;
import com.fundoo.notes.dto.request.ReminderUpdateRequestDto;
import com.fundoo.notes.dto.response.ReminderResponseDto;

import java.util.List;

/*
 * Class Name  : ReminderService
 * Description : Defines business operations for user reminders.
 * Author      : Utakarsh Jain
 * Date        : 26/11/2026
 */

public interface ReminderService {

    // Schedules a reminder for an authenticated user's note.
    ReminderResponseDto createReminder(
            String token,
            ReminderRequestDto request);

    // Retrieves all reminders belonging to the authenticated user.
    List<ReminderResponseDto> getAllReminders(String token);

    // Updates the time of an authenticated user's reminder.
    ReminderResponseDto updateReminder(
            Long reminderId,
            String token,
            ReminderUpdateRequestDto request);

    // Deletes an authenticated user's reminder.
    void deleteReminder(Long reminderId, String token);
}
