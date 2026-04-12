package com.astraval.coreflow.main_modules.analytics.dto;

public record PaymentFrequencyDto(
    String month,
    Long paymentCount
) {}
