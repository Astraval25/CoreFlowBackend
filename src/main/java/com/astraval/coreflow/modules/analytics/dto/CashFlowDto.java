package com.astraval.coreflow.modules.analytics.dto;

public record CashFlowDto(
    Double openingBalance,
    Double incoming,
    Double outgoing,
    Double closingBalance
) {}
