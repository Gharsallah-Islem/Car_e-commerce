package com.example.Backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    private String password; // Optional for updates

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @JsonAlias({ "fullName", "name" }) // Accept "fullName" or "name"
    private String fullName;

    // Support firstName and lastName for registration
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    private String phoneNumber;

    private String address;

    private Integer roleId;

    // Combine firstName and lastName into fullName if needed
    public String getFullName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return fullName;
    }
}
