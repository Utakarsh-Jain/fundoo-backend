
package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.request.LabelRequestDto;
import com.fundoo.notes.dto.response.LabelResponseDto;
import com.fundoo.notes.entity.Label;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.DuplicateLabelException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.exception.LabelNotFoundException;
import com.fundoo.notes.exception.NoteNotFoundException;
import com.fundoo.notes.repository.LabelRepository;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.LabelService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * Class Name  : LabelServiceImpl
 * Description : Implements user-specific label management using JWT.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

@Service
public class LabelServiceImpl implements LabelService {

    private static final Logger logger =
            LoggerFactory.getLogger(LabelServiceImpl.class);

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public LabelServiceImpl(
            LabelRepository labelRepository,
            NoteRepository noteRepository,
            UserRepository userRepository,
            TokenService tokenService) {

        this.labelRepository = labelRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public LabelResponseDto createLabel(
            String token,
            LabelRequestDto request) {

        User user = findUserFromToken(token);
        String name = request.getName().trim();

        if (labelRepository.findByNameIgnoreCaseAndUserId(
                name, user.getId()).isPresent()) {

            throw new DuplicateLabelException(
                    "Label already exists");
        }

        Label label = new Label(name, user);
        Label savedLabel = labelRepository.save(label);

        logger.info(
                "Label created successfully. Label ID: {}, User ID: {}",
                savedLabel.getId(),
                user.getId()
        );

        return mapToResponse(savedLabel);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public LabelResponseDto updateLabel(
            Long labelId,
            String token,
            LabelRequestDto request) {

        User user = findUserFromToken(token);
        String name = request.getName().trim();

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException(
                        "Label not found"));

        if (!label.getUser().getId().equals(user.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        if (labelRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(
                name, user.getId(), labelId)) {

            throw new DuplicateLabelException(
                    "Label already exists");
        }

        label.setName(name);
        Label savedLabel = labelRepository.save(label);

        logger.info(
                "Label renamed successfully. Label ID: {}, User ID: {}",
                labelId,
                user.getId()
        );

        return mapToResponse(savedLabel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelResponseDto> getAllLabels(String token) {

        User user = findUserFromToken(token);

        return labelRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void deleteLabel(Long labelId, String token) {

        User user = findUserFromToken(token);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException(
                        "Label not found"));

        if (!label.getUser().getId().equals(user.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        noteRepository.deleteLabelMappings(labelId);
        labelRepository.delete(label);

        logger.info(
                "Label deleted successfully. Label ID: {}, User ID: {}",
                labelId,
                user.getId()
        );
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

    // Converts a Label entity into its response DTO.
    private LabelResponseDto mapToResponse(Label label) {

        return new LabelResponseDto(
                label.getId(),
                label.getName()
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void assignLabelToNote(
            Long labelId,
            Long noteId,
            String token) {

        User user = findUserFromToken(token);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException(
                        "Label not found"));

        if (!label.getUser().getId().equals(user.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        Note note = noteRepository.findByIdAndUserId(
                noteId, user.getId()
        ).orElseThrow(() -> new NoteNotFoundException(
                "Note not found"));

        note.getLabels().add(label);
        noteRepository.save(note);

        logger.info(
                "Label {} assigned to Note {}",
                labelId,
                noteId
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "notes", allEntries = true)
    public void removeLabelFromNote(
            Long labelId,
            Long noteId,
            String token) {

        User user = findUserFromToken(token);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException(
                        "Label not found"));

        if (!label.getUser().getId().equals(user.getId())) {
            throw new LabelNotFoundException("Label not found");
        }

        Note note = noteRepository.findByIdAndUserId(
                noteId, user.getId()
        ).orElseThrow(() -> new NoteNotFoundException(
                "Note not found"));

        note.getLabels().remove(label);
        noteRepository.save(note);

        logger.info(
                "Label {} removed from Note {}",
                labelId,
                noteId
        );
    }
}
