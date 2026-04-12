package com.astraval.coreflow.main_modules.analytics.dto;

public record SalesPurchaseByItemDto(
    Long itemId,
    String itemName,
    Double totalQuantity,
    Double totalAmount
) {}
