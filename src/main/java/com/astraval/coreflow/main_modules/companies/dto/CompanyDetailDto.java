package com.astraval.coreflow.main_modules.companies.dto;

public record CompanyDetailDto(
    Long companyId,
    String companyName,
    String industry,
    String pan,
    String gstNo,
    String hsnCode,
    String shortName,
    String fsId,
    Boolean isActive) {
}
