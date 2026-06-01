package com.astraval.coreflow.main_modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserProfileDto {

    @NotBlank(message = "Username is required")
    private String userName;

    private String firstName;

    private String lastName;

    @Pattern(
            regexp = "^$|^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$",
            message = "Invalid email format")
    private String email;
}
