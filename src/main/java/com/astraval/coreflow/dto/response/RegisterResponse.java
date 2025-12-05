package com.astraval.coreflow.dto.response;

import lombok.Data;

@Data
public class RegisterResponse {
    private String userId;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private String companyId;
    private String companyName;
    private String industry;
    private String roleCode;
    private String accessToken;
    private String refreshToken;
}