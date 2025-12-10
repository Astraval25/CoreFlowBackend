package com.astraval.coreflow.shared.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.astraval.coreflow.shared.exception.SystemErrorException;
import com.astraval.coreflow.shared.util.ApiResponse;
import com.astraval.coreflow.shared.util.ApiResponseFactory;

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
