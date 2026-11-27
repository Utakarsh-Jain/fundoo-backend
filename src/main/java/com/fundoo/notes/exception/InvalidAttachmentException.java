package com.fundoo.notes.exception;

/*
 * Class Name  : InvalidAttachmentException
 * Description : Raised when an uploaded file fails validation or storage.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

public class InvalidAttachmentException extends RuntimeException {

    /**
     * Creates an invalid attachment exception.
     *
     * @param message the error message
     */
    public InvalidAttachmentException(String message) {
        super(message);
    }
}
