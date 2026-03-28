package com.astraval.coreflow.modules.analytics.dto;

public record PaymentModeDistributionDto(
    String mode,
    Double totalAmount,
    Long transactionCount,
    Double percentage
) {}
