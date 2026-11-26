
package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Class Name  : ReminderRepository
 * Description : Provides database operations for user reminders.
 * Author      : Utakarsh Jain
 * Date        : 26/11/2026
 */

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Retrieves a user's reminders in chronological order.
    List<Reminder> findByUserIdOrderByReminderTimeAsc(Long userId);

    // Verifies reminder ownership while fetching a reminder.
    Optional<Reminder> findByIdAndUserId(Long id, Long userId);

    // Checks whether a note already has a reminder for a user.
    Optional<Reminder> findByNoteIdAndUserId(Long noteId, Long userId);
}
