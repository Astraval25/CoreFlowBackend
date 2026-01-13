package com.astraval.coreflow.modules.orderdetails.dto;

import java.time.LocalDateTime;

public record OrderSummaryDto(
    Long orderId,
    String orderNumber,
    LocalDateTime orderDate,
    String sellerCompanyName,
    String buyerCompanyName,
    Double orderAmount,
    String orderStatus) {
}