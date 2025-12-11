package com.astraval.coreflow.modules.user.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private Integer userId;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
    private Integer companyId;
    private String companyName;
    private String industry;
    private String roleCode;
    private String accessToken;
    private String refreshToken;
}