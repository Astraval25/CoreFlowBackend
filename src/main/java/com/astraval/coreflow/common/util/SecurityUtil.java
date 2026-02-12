package com.astraval.coreflow.common.util;

import java.util.List;

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
            Claims claims = (Claims) details;
            String roleCode = claims.get("roleCode", String.class);
            if (roleCode != null) {
                return roleCode;
            }
            return claims.get("role", String.class);
        }
        return null;
    }
    
    public Integer getCurrentCompanyId() {
        Long companyId = getCurrentCompanyIdAsLong();
        return companyId != null ? companyId.intValue() : null;
    }

    public Long getCurrentCompanyIdAsLong() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Claims) {
            Claims claims = (Claims) details;

            Long directCompanyId = claims.get("companyId", Long.class);
            if (directCompanyId != null) {
                return directCompanyId;
            }

            Object defaultComp = claims.get("defaultComp");
            if (defaultComp instanceof List<?> defaultCompList && !defaultCompList.isEmpty()) {
                Object first = defaultCompList.get(0);
                if (first instanceof Integer intId) {
                    return intId.longValue();
                }
                if (first instanceof Long longId) {
                    return longId;
                }
                if (first instanceof String companyIdStr) {
                    try {
                        return Long.parseLong(companyIdStr);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
