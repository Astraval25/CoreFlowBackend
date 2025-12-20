package com.astraval.coreflow.common.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.astraval.coreflow.common.exception.DatabaseOperationException;
import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class DatabaseExceptionHandler {

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<ApiResponse<?>> handleDatabaseOperationError(DatabaseOperationException ex) {
        log.error("Database operation error", ex);
        return ResponseEntity.status(500).body(ApiResponseFactory.error(ex.getMessage(), 500));
    }
}
