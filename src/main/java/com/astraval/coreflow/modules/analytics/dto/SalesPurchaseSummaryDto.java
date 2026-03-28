package com.astraval.coreflow.modules.analytics.dto;

public record SalesPurchaseSummaryDto(
    Long totalOrders,
    Double totalAmount,
    Double totalPaid,
    Double totalDue,
    Double avgOrderValue
) {}
