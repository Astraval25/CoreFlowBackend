package com.astraval.coreflow.main_modules.analytics.dto;

public record PaymentModeDistributionDto(
    String mode,
    Double totalAmount,
    Long transactionCount,
    Double percentage
) {}
