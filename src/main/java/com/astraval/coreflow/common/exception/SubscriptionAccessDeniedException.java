package com.astraval.coreflow.common.exception;

import lombok.Getter;

@Getter
public class SubscriptionAccessDeniedException extends RuntimeException {

    private final String error;
    private final String featureCode;
    private final String requiredPlan;
    private final String currentPlan;
    private final String upgradeUrl;

    public SubscriptionAccessDeniedException(String message, String error, String featureCode, String requiredPlan,
            String currentPlan, String upgradeUrl) {
        super(message);
        this.error = error;
        this.featureCode = featureCode;
        this.requiredPlan = requiredPlan;
        this.currentPlan = currentPlan;
        this.upgradeUrl = upgradeUrl;
    }
}
