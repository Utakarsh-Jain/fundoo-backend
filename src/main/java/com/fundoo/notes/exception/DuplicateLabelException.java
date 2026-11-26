
package com.fundoo.notes.exception;

/*
 * Class Name  : DuplicateLabelException
 * Description : Handles duplicate label creation attempts.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

public class DuplicateLabelException extends RuntimeException {

    public DuplicateLabelException(String message) {
        super(message);
    }
}
