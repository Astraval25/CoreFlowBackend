package com.astraval.coreflow.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogUtil {
    
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    public static void setUserId(String userId) {
        MDC.put("userId", userId);
    }
    
    public static void setCompanyId(String companyId) {
        MDC.put("companyId", companyId);
    }
    
    public static void setRequestId(String requestId) {
        MDC.put("requestId", requestId);
    }
    
    public static void clear() {
        MDC.clear();
    }
}