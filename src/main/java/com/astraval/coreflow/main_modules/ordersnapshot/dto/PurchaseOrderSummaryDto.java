package com.astraval.coreflow.main_modules.ordersnapshot.dto;

import java.time.LocalDateTime;

public record PurchaseOrderSummaryDto(
                Long orderId,
                String orderNumber,
                LocalDateTime orderDate,
                String sellerCompanyName,
                String customerName,
                Double totalAmount,
                Double paidAmount,
                String orderStatus,
                Boolean isActive,
                String platformRef,
                String localOrderNumber) {
}