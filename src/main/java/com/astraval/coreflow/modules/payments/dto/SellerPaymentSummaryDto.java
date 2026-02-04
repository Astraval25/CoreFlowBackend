package com.astraval.coreflow.modules.payments.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerPaymentSummaryDto {
    private Long paymentId;
    private LocalDateTime paymentDate;
    private String orderIds;
    private Long paymentNumber;
    private Double amount;
    private String customerName;
    private String modeOfPayment;
    private String paymentStatus;
    private Boolean isActive;
    private String referenceNumber;
}
