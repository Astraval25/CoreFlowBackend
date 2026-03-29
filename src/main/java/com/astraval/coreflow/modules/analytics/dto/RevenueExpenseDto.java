package com.astraval.coreflow.modules.analytics.dto;

public record RevenueExpenseDto(
    String month,
    Double revenue,
    Double expense,
    Double netProfit,
    Double runningRevenue,
    Double runningExpense,
    Double runningNetProfit
) {}
