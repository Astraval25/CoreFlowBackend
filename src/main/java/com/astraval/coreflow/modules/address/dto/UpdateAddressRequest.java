package com.astraval.coreflow.modules.address.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateAddressRequest {
    private String attentionName;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    @NotBlank(message = "Address line 1 is required")
    private String line1;
    
    private String line2;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    private Integer pincode;
    private String phone;
    private String email;
}