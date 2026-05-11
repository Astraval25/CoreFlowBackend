package com.astraval.coreflow.main_modules.analytics.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentHistoryDto {
    private Long paymentId;
    private String paymentType;
    private LocalDateTime paymentDate;
    private String localPaymentNumber;
    private String paymentStatus;
    private String modeOfPayment;
    private Double amount;
}
