
package com.fundoo.notes.dto.response;

/*
 * Class Name  : LabelResponseDto
 * Description : Returns label details to the client.
 * Author      : Utakarsh Jain
 * Date        : 25/11/2026
 */

public class LabelResponseDto {

    private Long id;
    private String name;

    public LabelResponseDto() {
    }

    public LabelResponseDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Returns the label ID.
    public Long getId() {
        return id;
    }

    // Sets the label ID.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the label name.
    public String getName() {
        return name;
    }

    // Sets the label name.
    public void setName(String name) {
        this.name = name;
    }
}
