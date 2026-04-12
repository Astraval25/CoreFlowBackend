package com.astraval.coreflow.main_modules.analytics.dto;

public record CashFlowDto(
    String month,
    Double openingBalance,
    Double incoming,
    Double outgoing,
    Double closingBalance
) {}
