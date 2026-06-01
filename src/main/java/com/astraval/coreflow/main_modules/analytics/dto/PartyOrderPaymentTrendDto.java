package com.astraval.coreflow.main_modules.analytics.dto;

public record PartyOrderPaymentTrendDto(
    String day,
    Double totalQuantity,
    Double orderAmount,
    Double paidAmount
) {}
