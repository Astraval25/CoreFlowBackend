package com.astraval.coreflow.main_modules.companies.dto;

public record CompanySummaryDto(
    Long companyId,
    String companyName,
    String industry,
    String shortName,
    Boolean isActive) {
}