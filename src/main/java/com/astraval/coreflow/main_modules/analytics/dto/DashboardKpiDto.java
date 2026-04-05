package com.astraval.coreflow.main_modules.analytics.dto;

public record DashboardKpiDto(
    Double totalRevenue,
    Double totalExpense,
    Double netProfit,
    Long totalSalesOrders,
    Long totalPurchaseOrders,
    Long totalPaymentsReceived,
    Long totalPaymentsMade,
    Double avgOrderValue,
    Double outstandingReceivables,
    Double outstandingPayables
) {}
