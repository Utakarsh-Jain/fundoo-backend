package com.fundoo.notes.controller;

import com.fundoo.notes.dto.response.AttachmentResponseDto;
import com.fundoo.notes.service.AttachmentService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*
 * Class Name  : AttachmentController
 * Description : Exposes attachment upload, metadata, view and download APIs.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    /**
     * Creates the attachment controller with its service dependency.
     *
     * @param attachmentService the attachment service
     */
    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * Uploads a file and stores its metadata.
     *
     * @param token the Authorization header value
     * @param file the uploaded file
     * @return the stored attachment metadata
     */
    @PostMapping("/upload")
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {

        AttachmentResponseDto response =
                attachmentService.uploadAttachment(file, token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Lists all attachments owned by the authenticated user.
     *
     * @param token the Authorization header value
     * @return the attachment metadata list
     */
    @GetMapping
    public ResponseEntity<List<AttachmentResponseDto>> getAllAttachments(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                attachmentService.getAllAttachments(token)
        );
    }

    /**
     * Returns metadata for one owned attachment.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the attachment metadata
     */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponseDto> getAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                attachmentService.getAttachment(attachmentId, token)
        );
    }

    /**
     * Streams an attachment inline so the browser can display it.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the file bytes with inline content disposition
     */
    @GetMapping("/{attachmentId}/view")
    public ResponseEntity<byte[]> viewAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("Authorization") String token) {

        AttachmentResponseDto metadata =
                attachmentService.getAttachment(attachmentId, token);
        byte[] content =
                attachmentService.downloadAttachment(attachmentId, token);

        return buildFileResponse(metadata, content, false);
    }

    /**
     * Streams an attachment as a downloadable response.
     *
     * @param attachmentId the attachment identifier
     * @param token the Authorization header value
     * @return the file bytes with attachment content disposition
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("Authorization") String token) {

        AttachmentResponseDto metadata =
                attachmentService.getAttachment(attachmentId, token);
        byte[] content =
                attachmentService.downloadAttachment(attachmentId, token);

        return buildFileResponse(metadata, content, true);
    }

    /**
     * Builds a binary response with the correct media type and disposition.
     *
     * @param metadata the attachment metadata
     * @param content the file bytes
     * @param download whether the browser should download the file
     * @return the configured binary response
     */
    private ResponseEntity<byte[]> buildFileResponse(
            AttachmentResponseDto metadata,
            byte[] content,
            boolean download) {

        ContentDisposition disposition = download
                ? ContentDisposition.attachment()
                        .filename(metadata.getOriginalFileName())
                        .build()
                : ContentDisposition.inline()
                        .filename(metadata.getOriginalFileName())
                        .build();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(metadata.getContentType())
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(content);
    }
}
