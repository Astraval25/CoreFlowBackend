package com.astraval.coreflow.main_modules.Auth.dto;

// import com.astraval.coreflow.config.validation.ValidPAN;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    private String industry;
    
    // @ValidPAN(message = "Invalid Indian PAN format") // AAAAA9999A
    private String pan;
    
    private String firstName;
    
    private String lastName;
    
    private String userName;
    
    @Pattern(regexp = "^$|^[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,}$", message = "Invalid email format")
    private String email;

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^\\+?[0-9]{1,4}$", message = "Country code must contain only digits and optional leading +")
    private String countryCode;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "Phone number must be between 6 and 15 digits")
    private String phoneNumber;
    
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*\\d).{5,}$",
        message = "Password must be at least 5 characters and include a lowercase letter and a number"
    )
    private String password;
    
    
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        throw new IllegalArgumentException("Unknown field: " + key);
    }
}
