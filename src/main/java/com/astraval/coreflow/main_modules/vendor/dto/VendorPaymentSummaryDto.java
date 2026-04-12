package com.astraval.coreflow.main_modules.vendor.dto;

import java.time.LocalDateTime;

public record VendorPaymentSummaryDto(
    Long paymentId,
    String paymentPlatformRef,
    LocalDateTime paymentDate,
    Double amount
) {}
