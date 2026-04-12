package com.astraval.coreflow.main_modules.analytics.dto;

public record RevenueExpenseDto(
    String month,
    Double revenue,
    Double expense,
    Double netProfit,
    Double runningRevenue,
    Double runningExpense,
    Double runningNetProfit
) {}
