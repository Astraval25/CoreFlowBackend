package com.astraval.coreflow.common.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.astraval.coreflow.common.exception.SystemErrorException;
import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SystemExceptionHandler {
    
     @ExceptionHandler(SystemErrorException.class)
    public ResponseEntity<ApiResponse<?>> handleClientNotFound(SystemErrorException ex) {
        try {
            log.warn("System error: {}", ex.getMessage(), ex);
            ApiResponse<?> response = ApiResponseFactory.error(ex.getMessage(), 404);
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("Error handling SystemErrorException", e);
            ApiResponse<?> fallbackResponse = ApiResponseFactory.error("Internal server error", 500);
            return ResponseEntity.status(500).body(fallbackResponse);
        }
    }
}
