package com.astraval.coreflow.modules.analytics.dto;

public record PaymentFrequencyDto(
    String month,
    Long paymentCount
) {}
