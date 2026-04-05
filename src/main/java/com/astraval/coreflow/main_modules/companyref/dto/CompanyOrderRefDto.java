package com.astraval.coreflow.main_modules.companyref.dto;

public record CompanyOrderRefDto(
    Long companyOrderRefId,
    String localOrderNumber,
    String internalRemarks,
    String internalStatus,
    String internalTags,
    String customReference
) {}
