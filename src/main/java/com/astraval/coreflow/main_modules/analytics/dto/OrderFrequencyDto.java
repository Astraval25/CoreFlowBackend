package com.astraval.coreflow.main_modules.analytics.dto;

public record OrderFrequencyDto(
    String month,
    Long orderCount
) {}
