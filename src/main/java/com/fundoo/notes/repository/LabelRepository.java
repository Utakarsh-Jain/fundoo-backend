
package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
 * Interface Name : LabelRepository
 * Description    : Provides database operations for user labels.
 * Author         : Utakarsh Jain
 * Date           : 25/11/2026
 */

public interface LabelRepository extends JpaRepository<Label, Long> {

    // Fetches all labels belonging to a user.
    List<Label> findByUserId(Long userId);

    // Finds a label by name and user.
    Optional<Label> findByNameAndUserId(String name, Long userId);

    // Case-insensitive lookup used for duplicate-name validation.
    Optional<Label> findByNameIgnoreCaseAndUserId(String name, Long userId);

    // Checks another label of the same user already uses the name.
    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(
            String name, Long userId, Long id);
}
