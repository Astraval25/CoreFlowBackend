package com.astraval.coreflow.modules.ordersnapshot.dto;

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
                Boolean isActive) {
}