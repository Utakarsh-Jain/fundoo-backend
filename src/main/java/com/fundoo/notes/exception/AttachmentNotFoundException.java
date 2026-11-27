package com.fundoo.notes.exception;

/*
 * Class Name  : AttachmentNotFoundException
 * Description : Raised when an attachment or its stored file is missing.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

public class AttachmentNotFoundException extends RuntimeException {

    /**
     * Creates an attachment not found exception.
     *
     * @param message the error message
     */
    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
