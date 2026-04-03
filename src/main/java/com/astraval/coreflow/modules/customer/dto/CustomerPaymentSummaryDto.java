package com.astraval.coreflow.modules.customer.dto;

import java.time.LocalDateTime;

public record CustomerPaymentSummaryDto(
    Long paymentId,
    String paymentPlatformRef,
    LocalDateTime paymentDate,
    Double amount
) {}
