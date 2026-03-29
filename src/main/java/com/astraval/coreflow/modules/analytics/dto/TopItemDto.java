package com.astraval.coreflow.modules.analytics.dto;

public record TopItemDto(
    Long itemId,
    String itemName,
    Double totalAmount,
    Double totalQuantity
) {}
