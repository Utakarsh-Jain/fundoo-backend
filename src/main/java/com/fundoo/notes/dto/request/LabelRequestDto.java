
package com.fundoo.notes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * Class Name  : LabelRequestDto
 * Description : Validates label creation and rename requests.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

public class LabelRequestDto {

    @NotBlank(message = "Label name is required")
    @Size(max = 50, message = "Label name cannot exceed 50 characters")
    private String name;

    // Returns the label name.
    public String getName() {
        return name;
    }

    // Sets the label name.
    public void setName(String name) {
        this.name = name;
    }
}
