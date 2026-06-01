package com.astraval.coreflow.main_modules.analytics.dto;

public record EmployeeAnalyticsOverviewDto(
    Long employeeId,
    String employeeCode,
    String employeeName,
    Long approvedWorkLogCount,
    Double approvedWorkQuantity,
    Double approvedWorkAmount,
    Long approvedLeaveCount,
    Double approvedLeaveDays,
    Long pendingWorkLogCount,
    Long pendingLeaveLogCount
) {}
