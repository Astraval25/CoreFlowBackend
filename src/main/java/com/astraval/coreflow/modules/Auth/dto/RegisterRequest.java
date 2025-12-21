package com.astraval.coreflow.modules.Auth.dto;

import com.astraval.coreflow.config.validation.ValidPAN;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    @NotBlank(message = "Industry is required")
    private String industry;
    
    @ValidPAN(message = "Invalid Indian PAN format") // AAAAA9999A
    private String pan;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "User name is required")
    private String userName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
    regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
    message = "Password must contain upper, lower, number and special character"
    )
    private String password;
    
    
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        throw new IllegalArgumentException("Unknown field: " + key);
    }
}