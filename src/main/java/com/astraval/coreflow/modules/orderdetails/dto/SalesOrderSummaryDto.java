package com.astraval.coreflow.modules.orderdetails.dto;

import java.time.LocalDateTime;

public record SalesOrderSummaryDto(
    Long orderId,
    String orderNumber,
    LocalDateTime orderDate,
    String buyerCompanyName,
    String vendorName,
    Double totalAmount,
    Double paidAmount,
    String orderStatus,
    Boolean isActive,
    String platformRef,
    String localOrderNumber) {
}