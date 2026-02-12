package com.astraval.coreflow.common.exception.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.astraval.coreflow.common.exception.SubscriptionAccessDeniedException;
import com.astraval.coreflow.common.util.ApiResponse;

@RestControllerAdvice
public class SubscriptionExceptionHandler {

    @ExceptionHandler(SubscriptionAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleSubscriptionAccessDenied(
            SubscriptionAccessDeniedException ex) {

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("error", ex.getError());
        details.put("featureCode", ex.getFeatureCode());
        details.put("requiredPlan", ex.getRequiredPlan());
        details.put("currentPlan", ex.getCurrentPlan());
        details.put("upgradeUrl", ex.getUpgradeUrl());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                false,
                403,
                ex.getMessage(),
                details);

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
