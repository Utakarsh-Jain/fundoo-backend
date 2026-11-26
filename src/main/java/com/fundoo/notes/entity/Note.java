
package com.fundoo.notes.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;

/*
 * Class Name  : Note
 * Description : Represents a note created by a user and maps
 *               note details, labels and lifecycle states to the database.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean pinned = false;

    private boolean archived = false;

    private boolean trashed = false;

    // Many notes can belong to one user.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // A note can have multiple labels.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "note_labels",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();

    // Default constructor required by JPA.
    public Note() {
    }

    // Parameterized constructor for creating a note.
    public Note(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.pinned = false;
        this.archived = false;
        this.trashed = false;
    }

    // Returns the note ID.
    public Long getId() {
        return id;
    }

    // Sets the note ID.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the note title.
    public String getTitle() {
        return title;
    }

    // Sets the note title.
    public void setTitle(String title) {
        this.title = title;
    }

    // Returns the note description.
    public String getDescription() {
        return description;
    }

    // Sets the note description.
    public void setDescription(String description) {
        this.description = description;
    }

    // Checks whether the note is pinned.
    public boolean isPinned() {
        return pinned;
    }

    // Updates the pinned state.
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    // Checks whether the note is archived.
    public boolean isArchived() {
        return archived;
    }

    // Updates the archived state.
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    // Checks whether the note is trashed.
    public boolean isTrashed() {
        return trashed;
    }

    // Updates the trashed state.
    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    // Returns the user who owns this note.
    public User getUser() {
        return user;
    }

    // Assigns the note to a user.
    public void setUser(User user) {
        this.user = user;
    }

    // Returns labels assigned to the note.
    public Set<Label> getLabels() {
        return labels;
    }

    // Updates labels assigned to the note.
    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }
}
