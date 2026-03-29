package com.astraval.coreflow.modules.analytics.dto;

public record OrderFrequencyDto(
    String month,
    Long orderCount
) {}
