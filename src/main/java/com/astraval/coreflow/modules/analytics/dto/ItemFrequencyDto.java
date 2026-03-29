package com.astraval.coreflow.modules.analytics.dto;

public record ItemFrequencyDto(
    Long itemId,
    String itemName,
    Double totalQuantity,
    Long orderCount
) {}
