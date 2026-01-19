package com.astraval.coreflow.modules.address.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;

@Data
public class CreateUpdateAddressDto {
    private String attentionName;
    
    private String country;
    
    private String line1;
    
    private String line2;
    
    private String city;
    
    private String state;
    
    private Integer pincode;
    private String phone;
    
    @Email(message = "Invalid email format")
    private String email;
}