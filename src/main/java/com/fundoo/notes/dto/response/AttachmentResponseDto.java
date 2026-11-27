package com.fundoo.notes.dto.response;

import java.time.LocalDateTime;

/*
 * Class Name  : AttachmentResponseDto
 * Description : Carries safe attachment metadata to the client.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

public class AttachmentResponseDto {

    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;

    /**
     * Creates an empty attachment response instance.
     */
    public AttachmentResponseDto() {
    }

    /**
     * Creates an attachment response with complete metadata.
     *
     * @param id the attachment identifier
     * @param originalFileName the user-facing file name
     * @param contentType the MIME type
     * @param fileSize the file size in bytes
     * @param uploadedAt the upload timestamp
     */
    public AttachmentResponseDto(
            Long id,
            String originalFileName,
            String contentType,
            Long fileSize,
            LocalDateTime uploadedAt) {

        this.id = id;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Returns the attachment identifier.
     *
     * @return the attachment id
     */
    public Long getId() {
        return id;
    }

    /**
     * Updates the attachment identifier.
     *
     * @param id the attachment id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the user-facing file name.
     *
     * @return the original file name
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * Updates the user-facing file name.
     *
     * @param originalFileName the original file name
     */
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    /**
     * Returns the file MIME type.
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Updates the file MIME type.
     *
     * @param contentType the content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the file size in bytes.
     *
     * @return the file size
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Updates the file size in bytes.
     *
     * @param fileSize the file size
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Returns the upload timestamp.
     *
     * @return the upload time
     */
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Updates the upload timestamp.
     *
     * @param uploadedAt the upload time
     */
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
