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
    String contactPerson,
    String contactEmail,
    String contactPhone,
    String website,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String country,
    String postalCode,
    String publicDescription,
    Boolean isActive) {
}
