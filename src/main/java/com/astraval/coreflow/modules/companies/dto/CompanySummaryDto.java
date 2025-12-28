package com.astraval.coreflow.modules.companies.dto;

public record CompanySummaryDto(
    Long companyId,
    String companyName,
    String industry,
    String shortName,
    Boolean isActive) {
}