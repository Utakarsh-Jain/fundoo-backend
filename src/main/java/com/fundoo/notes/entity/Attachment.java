package com.fundoo.notes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/*
 * Class Name  : Attachment
 * Description : Stores uploaded file metadata and its owner.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * Creates an empty attachment instance for JPA.
     */
    public Attachment() {
    }

    /**
     * Creates an attachment with complete file metadata.
     *
     * @param user the owner of the attachment
     * @param originalFileName the name shown to the user
     * @param storedFileName the unique name used on disk
     * @param contentType the MIME type of the uploaded file
     * @param fileSize the size of the uploaded file in bytes
     * @param filePath the absolute path of the stored file
     * @param uploadedAt the upload timestamp
     */
    public Attachment(
            User user,
            String originalFileName,
            String storedFileName,
            String contentType,
            Long fileSize,
            String filePath,
            LocalDateTime uploadedAt) {

        this.user = user;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Returns the attachment identifier.
     *
     * @return the database identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * Updates the attachment identifier.
     *
     * @param id the database identifier
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the owner of the attachment.
     *
     * @return the owning user
     */
    public User getUser() {
        return user;
    }

    /**
     * Updates the owner of the attachment.
     *
     * @param user the owning user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the original file name.
     *
     * @return the user-facing file name
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * Updates the original file name.
     *
     * @param originalFileName the user-facing file name
     */
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    /**
     * Returns the generated storage file name.
     *
     * @return the unique disk file name
     */
    public String getStoredFileName() {
        return storedFileName;
    }

    /**
     * Updates the generated storage file name.
     *
     * @param storedFileName the unique disk file name
     */
    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
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
     * Returns the absolute storage path.
     *
     * @return the stored file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Updates the absolute storage path.
     *
     * @param filePath the stored file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
