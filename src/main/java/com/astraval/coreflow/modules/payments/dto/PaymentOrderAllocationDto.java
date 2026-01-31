package com.astraval.coreflow.modules.payments.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderAllocationDto {
    private Long paymentOrderAllocationId;
    private Long orderId;
    private String orderNumber;
    private Double amountApplied;
    private LocalDateTime allocationDate;
    private String allocationRemarks;
    private Boolean isActive;
}