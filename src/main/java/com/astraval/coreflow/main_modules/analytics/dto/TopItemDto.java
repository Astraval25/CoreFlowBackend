package com.astraval.coreflow.main_modules.analytics.dto;

public record TopItemDto(
    Long itemId,
    String itemName,
    Double totalAmount,
    Double totalQuantity
) {}
