package com.astraval.coreflow.modules.vendor.dto;

public record VendorSummaryDto(
    Long vendorId,
    String displayName,
    String vendorCompanyName,
    String email,
    Boolean isActive) {
}