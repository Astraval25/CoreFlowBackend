package com.astraval.coreflow.main_modules.analytics.dto;

public record MonthlyTrendDto(
    String month,
    Double salesAmount,
    Double purchaseAmount,
    Double paymentReceived,
    Double paymentMade
) {}
