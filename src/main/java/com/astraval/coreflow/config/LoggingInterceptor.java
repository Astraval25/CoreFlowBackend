package com.astraval.coreflow.config;

import com.astraval.coreflow.common.util.LogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger log = LogUtil.getLogger(LoggingInterceptor.class);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        LogUtil.setRequestId(requestId);
        
        log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("Request completed: {} {} - Status: {}", 
                request.getMethod(), request.getRequestURI(), response.getStatus());
        LogUtil.clear();
    }
}