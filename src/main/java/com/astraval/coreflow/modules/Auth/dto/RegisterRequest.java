package com.astraval.coreflow.modules.Auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String companyName;
    private String industry;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String password;
}