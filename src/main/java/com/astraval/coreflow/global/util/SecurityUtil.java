package com.astraval.coreflow.global.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;

@Service
public class SecurityUtil {
    
    public String getCurrentSub() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    public String getCurrentRoleCode() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Claims) {
            return ((Claims) details).get("roleCode", String.class);
        }
        return null;
    }
    
    public Integer getCurrentCompanyId() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Claims) {
            return ((Claims) details).get("companyId", Integer.class);
        }
        return null;
    }
}
