package com.astraval.coreflow.modules.analytics.dto;

public record CashFlowDto(
    String month,
    Double openingBalance,
    Double incoming,
    Double outgoing,
    Double closingBalance
) {}
