package com.astraval.coreflow.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// Logging functionality moved to RequestLoggingFilter to avoid duplicate logs
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    // No logging here - handled by RequestLoggingFilter
}