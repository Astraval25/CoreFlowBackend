package com.astraval.coreflow.main_modules.companyref.dto;

public record CompanyPaymentRefDto(
    Long companyPaymentRefId,
    String localPaymentNumber,
    String internalRemarks,
    String internalStatus,
    String customReference
) {}
