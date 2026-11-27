package com.fundoo.notes.dto.response;

/*
 * Class Name  : UserProfileResponseDto
 * Description : Represents safe profile details of the authenticated user.
 * Author      : Utakarsh Jain
 * Date        : 27/11/2026
 */

public class UserProfileResponseDto {

    private String firstName;
    private String email;

    // Default constructor.
    public UserProfileResponseDto() {
    }

    // Initializes the profile response.
    public UserProfileResponseDto(String firstName, String email) {
        this.firstName = firstName;
        this.email = email;
    }

    // Returns the first name.
    public String getFirstName() {
        return firstName;
    }

    // Sets the first name.
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Returns the email address.
    public String getEmail() {
        return email;
    }

    // Sets the email address.
    public void setEmail(String email) {
        this.email = email;
    }
}
