package com.astraval.coreflow.main_modules.analytics.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderHistoryDto {
    private Long orderId;
    private String orderType;
    private LocalDateTime orderDate;
    private String partyName;
    private String localOrderNumber;
    private String orderStatus;
    private Double totalItemQuantity;
    private Double totalAmount;
    private Double paidAmount;
    private Integer paidPercentage;
}
