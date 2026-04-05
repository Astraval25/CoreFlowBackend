package com.astraval.coreflow.main_modules.vendor.dto;

import java.time.LocalDateTime;

public record VendorOrderSummaryDto(
    Long orderId,
    String orderNumber,
    Double totalAmount,
    String orderPlatformRef,
    Double paidAmount,
    LocalDateTime orderDate
) {}
