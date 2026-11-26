
package com.fundoo.notes.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * Class Name  : Reminder
 * Description : Represents a scheduled reminder for a user's note.
 * Author      : Utakarsh Jain
 * Date        : 26/11/2026
 */

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime reminderTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    public Reminder() {
    }

    public Reminder(Note note, User user, LocalDateTime reminderTime) {
        this.note = note;
        this.user = user;
        this.reminderTime = reminderTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalDateTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }
}
