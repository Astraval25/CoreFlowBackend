package com.astraval.coreflow.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;

@Service
public class SecurityUtil {
    
    public String getCurrentSub() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    public String getCurrentRoleCode() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Claims) {
            return ((Claims) principal).get("roleCode", String.class);
        }
        return null;
    }
}
