package com.astraval.coreflow.common.exception.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<Map<String, String>> handleValidation(
      MethodArgumentNotValidException ex) {

    FieldError error = ex.getBindingResult().getFieldError(); // first error only

    Map<String, String> response = new HashMap<>();
    response.put(error.getField(), error.getDefaultMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));

    return ApiResponseFactory.validation(errors, "Invalid Input");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<String>> handleUnknownField(
      IllegalArgumentException ex) {

    return ResponseEntity.badRequest()
        .body(new ApiResponse<>(false, 400, ex.getMessage(), null));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<String>> handleParseError(HttpMessageNotReadableException ex) {
    return new ResponseEntity<>(
        new ApiResponse<>(false, 400, "Malformed JSON request", ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

}