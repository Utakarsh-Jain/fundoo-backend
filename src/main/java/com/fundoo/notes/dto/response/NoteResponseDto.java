
package com.fundoo.notes.dto.response;

import java.util.List;

/*
 * Class Name  : NoteResponseDto
 * Description : Represents note details and assigned labels
 *               returned to the client.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

public class NoteResponseDto {

    private Long id;
    private String title;
    private String description;
    private boolean pinned;
    private boolean archived;
    private boolean trashed;
    private List<LabelResponseDto> labels;

    public NoteResponseDto() {
    }

    public NoteResponseDto(
            Long id,
            String title,
            String description,
            boolean pinned,
            boolean archived,
            boolean trashed,
            List<LabelResponseDto> labels) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.pinned = pinned;
        this.archived = archived;
        this.trashed = trashed;
        this.labels = labels;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public List<LabelResponseDto> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelResponseDto> labels) {
        this.labels = labels;
    }
}
