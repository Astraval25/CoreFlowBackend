package com.astraval.coreflow.modules.analytics.dto;

public record SalesPurchaseByItemDto(
    Long itemId,
    String itemName,
    Double totalQuantity,
    Double totalAmount
) {}
