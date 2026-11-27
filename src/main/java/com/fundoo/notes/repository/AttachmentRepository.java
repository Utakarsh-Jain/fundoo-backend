package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Interface Name : AttachmentRepository
 * Description    : Provides database access for user attachments.
 * Author         : Utakarsh Jain
 * Date           : 27/11/2026
 */

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Finds every attachment owned by a user, newest first.
     *
     * @param userId the owner identifier
     * @return the matching attachments
     */
    List<Attachment> findByUserIdOrderByUploadedAtDesc(Long userId);

    /**
     * Finds one attachment by identifier and owner.
     *
     * @param id the attachment identifier
     * @param userId the owner identifier
     * @return the matching attachment when it belongs to the user
     */
    Optional<Attachment> findByIdAndUserId(Long id, Long userId);
}
