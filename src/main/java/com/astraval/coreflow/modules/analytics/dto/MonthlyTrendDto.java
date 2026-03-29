package com.astraval.coreflow.modules.analytics.dto;

public record MonthlyTrendDto(
    String month,
    Double salesAmount,
    Double purchaseAmount,
    Double paymentReceived,
    Double paymentMade
) {}
