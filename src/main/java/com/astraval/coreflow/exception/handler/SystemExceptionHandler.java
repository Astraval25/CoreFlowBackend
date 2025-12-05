package com.astraval.coreflow.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.astraval.coreflow.dto.response.ApiResponse;
import com.astraval.coreflow.exception.SystemErrorException;
import com.astraval.coreflow.util.ApiResponseFactory;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SystemExceptionHandler {
    
     @ExceptionHandler(SystemErrorException.class)
    public ResponseEntity<ApiResponse<?>> handleClientNotFound(SystemErrorException ex) {
        log.warn("System error: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponseFactory.error(ex.getMessage(), 404);
        return ResponseEntity.status(500).body(response);
    }
}
