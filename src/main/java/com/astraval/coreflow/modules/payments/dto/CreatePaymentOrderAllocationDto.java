package com.astraval.coreflow.modules.payments.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePaymentOrderAllocationDto {

    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Amount applied is required")
    @Positive(message = "Amount applied must be positive")
    private Double amountApplied;
    
    private LocalDateTime allocationDate; // eg: 2024-01-15T10:30:00
    
    private String allocationRemarks;
}