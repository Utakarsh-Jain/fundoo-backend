
/*
 * Class Name  : NoteRepository
 * Description : Provides database operations for Note entities,
 *               including user-specific retrieval and filtering.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Retrieves all notes belonging to a particular user.
    List<Note> findByUserId(Long userId);

    // Retrieves active notes belonging to a user.
    List<Note> findByUserIdAndArchivedFalseAndTrashedFalse(Long userId);

    // Searches a user's notes by title, ignoring letter case.
    List<Note> findByUserIdAndTitleContainingIgnoreCase(
            Long userId,
            String keyword
    );

    // Verifies note ownership.
    Optional<Note> findByIdAndUserId(Long id, Long userId);

    // Removes label mappings before deleting a label.
    @Modifying
    @Query(
            value = "DELETE FROM note_labels WHERE label_id = :labelId",
            nativeQuery = true
    )
    void deleteLabelMappings(@Param("labelId") Long labelId);
}
