package com.astraval.coreflow.main_modules.analytics.dto;

public record SalesPurchaseSummaryDto(
    Long totalOrders,
    Double totalAmount,
    Double totalPaid,
    Double totalDue,
    Double avgOrderValue
) {}
