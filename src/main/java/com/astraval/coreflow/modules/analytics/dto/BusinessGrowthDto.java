package com.astraval.coreflow.modules.analytics.dto;

public record BusinessGrowthDto(
    Double currentPeriodAmount,
    Double previousPeriodAmount,
    Double growthPercentage
) {}
