package com.astraval.coreflow.modules.analytics.dto;

public record ProfitByItemDto(
    Long itemId,
    String itemName,
    Double totalSalesAmount,
    Double totalPurchaseAmount,
    Double profit,
    Double profitMargin
) {}
