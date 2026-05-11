package com.astraval.coreflow.main_modules.marketplace.dto;

public record MarketplaceCompanyDto(
        Long companyId,
        String companyName,
        String industry,
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
        String publicDescription) {
}
