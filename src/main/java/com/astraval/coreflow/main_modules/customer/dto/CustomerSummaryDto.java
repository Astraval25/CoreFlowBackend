package com.astraval.coreflow.main_modules.customer.dto;

public record CustomerSummaryDto(
    Long customerId,
    String displayName,
    String customerCompanyName,
    String email,
    Double dueAmount,
    Boolean isActive) {
}
