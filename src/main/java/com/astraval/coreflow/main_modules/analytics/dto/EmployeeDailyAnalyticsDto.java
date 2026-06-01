package com.astraval.coreflow.main_modules.analytics.dto;

public record EmployeeDailyAnalyticsDto(
    String day,
    Double approvedWorkQuantity,
    Double approvedWorkAmount,
    Double approvedLeaveDays
) {}
