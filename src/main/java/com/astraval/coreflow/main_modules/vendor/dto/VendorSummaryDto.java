package com.astraval.coreflow.main_modules.vendor.dto;

public record VendorSummaryDto(
    Long vendorId,
    String displayName,
    String vendorCompanyName,
    String email,
    Double dueAmount,
    Boolean isActive){
}