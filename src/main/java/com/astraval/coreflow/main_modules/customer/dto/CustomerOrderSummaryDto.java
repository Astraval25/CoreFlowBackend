package com.astraval.coreflow.main_modules.customer.dto;

import java.time.LocalDateTime;

public record CustomerOrderSummaryDto(
    Long orderId,
    String orderNumber,
    Double totalAmount,
    String orderPlatformRef,
    Double paidAmount,
    LocalDateTime orderDate
) {}
