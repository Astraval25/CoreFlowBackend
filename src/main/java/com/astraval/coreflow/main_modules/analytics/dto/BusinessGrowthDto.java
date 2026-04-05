package com.astraval.coreflow.main_modules.analytics.dto;

public record BusinessGrowthDto(
    String month,
    Double currentMonthAmount,
    Double runningTotal,
    Double previousMonthAmount,
    Double growthPercentage
) {}
