package com.astraval.coreflow.global.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.astraval.coreflow.global.exception.InvalidCredentialsException;
import com.astraval.coreflow.global.exception.UserNotActiveException;
import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Auth error: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponseFactory.error(ex.getMessage(), 400);
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotActive(UserNotActiveException ex) {
        log.warn("User not active: {}", ex.getMessage());
        ApiResponse<?> response = ApiResponseFactory.error(ex.getMessage(), 403);
        return ResponseEntity.status(403).body(response);
    }
}
