package com.astraval.coreflow.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.astraval.coreflow.dto.response.ApiResponse;
import com.astraval.coreflow.exception.AdminAccessDeniedException;
import com.astraval.coreflow.exception.AdminNotFoundException;
import com.astraval.coreflow.util.ApiResponseFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class AdminExceptionHandler {

    @ExceptionHandler(AdminAccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAdminAccessDenied(AdminAccessDeniedException ex) {
        log.warn("Admin access denied: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponseFactory.error(ex.getMessage(), 403);
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleAdminNotFound(AdminNotFoundException ex) {
        log.warn("Admin not found: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponseFactory.error(ex.getMessage(), 404);
        return ResponseEntity.status(404).body(response);
    }
}
