
package com.fundoo.notes.controller;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.service.LabelService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Class Name  : LabelController
 * Description : Exposes REST APIs for managing user labels.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    // Creates a label for the authenticated user.
    @PostMapping
    public ResponseEntity<LabelResponseDto> createLabel(
            @Valid @RequestBody LabelRequestDto request,
            @RequestHeader("Authorization") String token) {

        LabelResponseDto response =
                labelService.createLabel(token, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Renames a label belonging to the authenticated user.
    @PutMapping("/{labelId}")
    public ResponseEntity<LabelResponseDto> updateLabel(
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequestDto request,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                labelService.updateLabel(labelId, token, request)
        );
    }

    // Retrieves all labels belonging to the authenticated user.
    @GetMapping
    public ResponseEntity<List<LabelResponseDto>> getAllLabels(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                labelService.getAllLabels(token)
        );
    }

    // Deletes a label belonging to the authenticated user.
    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(
            @PathVariable Long labelId,
            @RequestHeader("Authorization") String token) {

        labelService.deleteLabel(labelId, token);

        return ResponseEntity.noContent().build();
    }

    // Assigns a label to a note.
    @PostMapping("/{labelId}/notes/{noteId}")
    public ResponseEntity<Void> assignLabelToNote(
            @PathVariable Long labelId,
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        labelService.assignLabelToNote(labelId, noteId, token);

        return ResponseEntity.noContent().build();
    }

    // Removes a label from a note.
    @DeleteMapping("/{labelId}/notes/{noteId}")
    public ResponseEntity<Void> removeLabelFromNote(
            @PathVariable Long labelId,
            @PathVariable Long noteId,
            @RequestHeader("Authorization") String token) {

        labelService.removeLabelFromNote(labelId, noteId, token);

        return ResponseEntity.noContent().build();
    }

}
