package com.astraval.coreflow.modules.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String roleCode;
    private String landingUrl;
    private String companyId;
    private String companyName;
    
    public LoginResponse(String token, String refreshToken, String userId, String roleCode, String landingUrl, String companyId, String companyName) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.roleCode = roleCode;
        this.landingUrl = landingUrl;
        this.companyId = companyId;
        this.companyName = companyName;
    }
}