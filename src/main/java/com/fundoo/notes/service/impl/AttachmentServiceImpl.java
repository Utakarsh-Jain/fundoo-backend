package com.fundoo.notes.service.impl;

import com.fundoo.notes.dto.response.AttachmentResponseDto;
import com.fundoo.notes.entity.Attachment;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.exception.AttachmentNotFoundException;
import com.fundoo.notes.exception.InvalidAttachmentException;
import com.fundoo.notes.exception.InvalidCredentialsException;
import com.fundoo.notes.repository.AttachmentRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.service.AttachmentService;
import com.fundoo.notes.service.TokenService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * Class Name  : AttachmentServiceImpl
 * Description : Implements attachment validation, storage and retrieval.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger logger =
            LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final Path storageDirectory;
    private final long maxFileSize;
    private final Set<String> allowedContentTypes;

    /**
     * Creates the attachment service with configured storage rules.
     *
     * @param attachmentRepository the attachment repository
     * @param userRepository the user repository
     * @param tokenService the token validation service
     * @param storageDir the configured upload directory
     * @param maxSizeMb the maximum upload size in megabytes
     * @param allowedTypes the comma-separated allowed MIME types
     */
    public AttachmentServiceImpl(
            AttachmentRepository attachmentRepository,
            UserRepository userRepository,
            TokenService tokenService,
            @Value("${app.attachments.storage-dir:./uploads}") String storageDir,
            @Value("${app.attachments.max-size-mb:10}") long maxSizeMb,
            @Value("${app.attachments.allowed-types:image/png,image/jpeg,image/gif,application/pdf,text/plain,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document}") String allowedTypes) {

        this.attachmentRepository = attachmentRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.storageDirectory = Paths.get(storageDir)
                .toAbsolutePath()
                .normalize();
        this.maxFileSize = maxSizeMb * 1024L * 1024L;
        this.allowedContentTypes = Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .map(type -> type.toLowerCase(Locale.ROOT))
                .filter(type -> !type.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Validates, stores and records an uploaded file for the current user.
     *
     * @param file the multipart upload
     * @param token the Authorization header value
     * @return the stored attachment metadata
     */
    @Override
    @Transactional
    public AttachmentResponseDto uploadAttachment(
            MultipartFile file,
            String token) {

        logger.info("Attachment upload requested");

        User user = findUserFromToken(token);
        String originalFileName = validateFile(file);
        String contentType = file.getContentType()
                .toLowerCase(Locale.ROOT);
        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path targetPath = buildStoragePath(storedFileName);

        try {
            Files.createDirectories(storageDirectory);
            file.transferTo(targetPath);
        } catch (IOException exception) {
            logger.error("Failed to store attachment: {}", exception.getMessage());
            throw new InvalidAttachmentException("Failed to store attachment");
        }

        Attachment attachment = new Attachment(
                user,
                originalFileName,
                storedFileName,
                contentType,
                file.getSize(),
                targetPath.toString(),
                LocalDateTime.now()
        );

        Attachment savedAttachment = attachmentRepository.save(attachment);

        logger.info(
                "Attachment stored successfully. Attachment ID: {}",
                savedAttachment.getId()
        );

        return mapToResponse(savedAttachment);
    }

    /**
     * Lists all attachment metadata owned by the current user.
     *
     * @param token the Authorization header value
     * @return the attachment metadata list
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getAllAttachments(String token) {

        User user = findUserFromToken(token);

        logger.info(
                "Fetching attachments for User ID: {}",
                user.getId()
        );

        return attachmentRepository
                .findByUserIdOrderByUploadedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Returns metadata for one owned attachment.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the attachment metadata
     */
    @Override
    @Transactional(readOnly = true)
    public AttachmentResponseDto getAttachment(
            Long attachmentId,
            String token) {

        Attachment attachment = findAttachmentForUser(attachmentId, token);

        return mapToResponse(attachment);
    }

    /**
     * Reads the stored bytes of one owned attachment.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the file bytes
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadAttachment(
            Long attachmentId,
            String token) {

        Attachment attachment = findAttachmentForUser(attachmentId, token);
        Path filePath = Paths.get(attachment.getFilePath())
                .toAbsolutePath()
                .normalize();

        if (!filePath.startsWith(storageDirectory)) {
            throw new InvalidAttachmentException(
                    "Invalid attachment storage path"
            );
        }

        if (!Files.exists(filePath)) {
            throw new AttachmentNotFoundException(
                    "Attachment file is missing"
            );
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException exception) {
            logger.error(
                    "Failed to read attachment {}: {}",
                    attachmentId,
                    exception.getMessage()
            );
            throw new InvalidAttachmentException(
                    "Failed to read attachment"
            );
        }
    }

    /**
     * Checks the upload and returns a safe original file name.
     *
     * @param file the multipart upload
     * @return the sanitized original file name
     */
    private String validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidAttachmentException("File is required");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !allowedContentTypes.contains(
                        contentType.toLowerCase(Locale.ROOT))) {

            throw new InvalidAttachmentException(
                    "Unsupported file type"
            );
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidAttachmentException(
                    "File size must not exceed " +
                            (maxFileSize / (1024L * 1024L)) + " MB"
            );
        }

        return sanitizeFileName(file.getOriginalFilename());
    }

    /**
     * Removes directory information and unsafe characters from a file name.
     *
     * @param fileName the client-provided file name
     * @return the sanitized file name
     */
    private String sanitizeFileName(String fileName) {

        if (fileName == null || fileName.isBlank()) {
            throw new InvalidAttachmentException("File name is required");
        }

        String normalizedName = fileName
                .replace('\\', '/')
                .substring(fileName.replace('\\', '/').lastIndexOf('/') + 1);
        String safeName = normalizedName.replaceAll("[^a-zA-Z0-9._-]", "_");

        if (safeName.isBlank() || safeName.equals(".") || safeName.equals("..")) {
            throw new InvalidAttachmentException("Invalid file name");
        }

        return safeName;
    }

    /**
     * Builds and validates a storage path inside the configured directory.
     *
     * @param storedFileName the generated file name
     * @return the validated absolute path
     */
    private Path buildStoragePath(String storedFileName) {

        Path targetPath = storageDirectory
                .resolve(storedFileName)
                .normalize();

        if (!targetPath.startsWith(storageDirectory)) {
            throw new InvalidAttachmentException(
                    "Invalid attachment storage path"
            );
        }

        return targetPath;
    }

    /**
     * Finds an attachment and verifies that it belongs to the current user.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the owned attachment
     */
    private Attachment findAttachmentForUser(
            Long attachmentId,
            String token) {

        User user = findUserFromToken(token);

        return attachmentRepository
                .findByIdAndUserId(attachmentId, user.getId())
                .orElseThrow(() -> new AttachmentNotFoundException(
                        "Attachment not found"
                ));
    }

    /**
     * Resolves the authenticated user from the Authorization header.
     *
     * @param token the Authorization header value
     * @return the authenticated user
     */
    private User findUserFromToken(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException(
                    "Authorization token is required"
            );
        }

        String jwt = token.startsWith("Bearer ")
                ? token.substring(7).trim()
                : token.trim();

        if (!tokenService.isTokenValid(jwt)) {
            throw new InvalidCredentialsException(
                    "Invalid or expired token"
            );
        }

        String email = tokenService.extractSubject(jwt);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "User associated with token not found"
                ));
    }

    /**
     * Converts an attachment entity into its client-safe response.
     *
     * @param attachment the stored attachment
     * @return the attachment metadata response
     */
    private AttachmentResponseDto mapToResponse(Attachment attachment) {

        return new AttachmentResponseDto(
                attachment.getId(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedAt()
        );
    }
}
