package com.astraval.coreflow.modules.analytics.dto;

public record RunningAmountDto(
    String month,
    Double cumulativeAmount
) {}
