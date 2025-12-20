package com.astraval.coreflow.modules.Auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Integer userId;
    private String roleCode;
    private String landingUrl;
    private Integer companyId;
    private String companyName;
    private List<Long> companyIds;
    
    public LoginResponse(String token, String refreshToken, Integer userId, String roleCode, String landingUrl, Integer companyId, String companyName, List<Long> companyIds) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.roleCode = roleCode;
        this.landingUrl = landingUrl;
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyIds = companyIds;
    }
    
    public LoginResponse(String token, String refreshToken, Integer userId, String roleCode, String landingUrl, Integer companyId, String companyName) {
        this(token, refreshToken, userId, roleCode, landingUrl, companyId, companyName, null);
    }
}