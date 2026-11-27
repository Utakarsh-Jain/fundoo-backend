package com.fundoo.notes.service;

import com.fundoo.notes.dto.response.AttachmentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * Interface Name : AttachmentService
 * Description    : Defines attachment storage, metadata and retrieval operations.
 * Author         : Utakarsh Jain
 * Date           : 27/11/2026
 */

public interface AttachmentService {

    /**
     * Validates, stores and records an uploaded file.
     *
     * @param file the multipart upload
     * @param token the Authorization header value
     * @return the stored attachment metadata
     */
    AttachmentResponseDto uploadAttachment(MultipartFile file, String token);

    /**
     * Lists all attachment metadata for the authenticated user.
     *
     * @param token the Authorization header value
     * @return the attachment metadata list
     */
    List<AttachmentResponseDto> getAllAttachments(String token);

    /**
     * Returns metadata for one attachment owned by the authenticated user.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the attachment metadata
     */
    AttachmentResponseDto getAttachment(Long attachmentId, String token);

    /**
     * Reads the stored bytes of one attachment owned by the user.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the file bytes
     */
    byte[] downloadAttachment(Long attachmentId, String token);
}
