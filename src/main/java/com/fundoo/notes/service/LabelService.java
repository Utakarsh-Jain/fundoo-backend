
package com.fundoo.notes.service;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;

import java.util.List;

/*
 * Interface Name : LabelService
 * Description    : Defines operations for managing user labels.
 * Author         : Utakarsh Jain
 * Date           : 25/11/2026
 */

public interface LabelService {

    // Creates a new label for the authenticated user.
    LabelResponseDto createLabel(String token, LabelRequestDto request);

    // Renames a label belonging to the authenticated user.
    LabelResponseDto updateLabel(
            Long labelId, String token, LabelRequestDto request);

    // Fetches all labels belonging to the authenticated user.
    List<LabelResponseDto> getAllLabels(String token);

    // Deletes a label belonging to the authenticated user.
    void deleteLabel(Long labelId, String token);

    // Assigns a label to a user's note.
    void assignLabelToNote(Long labelId, Long noteId, String token);

    // Removes a label from a user's note.
    void removeLabelFromNote(Long labelId, Long noteId, String token);
}
