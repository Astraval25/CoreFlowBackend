package com.astraval.coreflow.modules.analytics.dto;

public record SalesPurchaseByPartyDto(
    Long partyId,
    String partyName,
    Long totalOrders,
    Double totalAmount,
    Double paidAmount,
    Double dueAmount
) {}
