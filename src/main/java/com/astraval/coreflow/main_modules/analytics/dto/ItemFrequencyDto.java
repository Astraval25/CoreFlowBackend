package com.astraval.coreflow.main_modules.analytics.dto;

public record ItemFrequencyDto(
    Long itemId,
    String itemName,
    Double totalQuantity,
    Long orderCount
) {}
