package com.astraval.coreflow.main_modules.Auth.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^\\+?[0-9]{1,4}$", message = "Country code must contain only digits and optional leading +")
    private String countryCode;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "Phone number must be between 6 and 15 digits")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;
    
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        throw new IllegalArgumentException("Unknown field: " + key);
    }
}
