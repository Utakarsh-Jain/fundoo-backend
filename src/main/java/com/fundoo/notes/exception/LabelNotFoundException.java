
package com.fundoo.notes.exception;

/*
 * Class Name  : LabelNotFoundException
 * Description : Handles missing labels and unauthorized label access.
 * Author      : Utakarsh Jain
 * Date         : 25/11/2026
 */

public class LabelNotFoundException extends RuntimeException {

    public LabelNotFoundException(String message) {
        super(message);
    }
}
