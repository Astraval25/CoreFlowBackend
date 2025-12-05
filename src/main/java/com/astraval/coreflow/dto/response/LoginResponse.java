package com.astraval.coreflow.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String roleCode;
    private String landingUrl;
    
    public LoginResponse(String token, String refreshToken, String userId, String roleCode, String landingUrl) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.roleCode = roleCode;
        this.landingUrl = landingUrl;
    }
}