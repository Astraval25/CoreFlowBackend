package com.astraval.coreflow.modules.customer.dto;

public record CustomerSummaryDto(
    Long customerId,
    String displayName,
    String customerCompanyName,
    String email,
    Boolean isActive) {
}