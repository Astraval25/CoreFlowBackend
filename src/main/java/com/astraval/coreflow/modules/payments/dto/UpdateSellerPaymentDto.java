package com.astraval.coreflow.modules.payments.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateSellerPaymentDto {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;
    
    @NotNull(message = "Mode of payment is required")
    private String modeOfPayment;
    
    private String referenceNumber;
    
    private String paymentRemarks;
    
    private String paymentProofFsId;
    
    @Valid
    private List<UpdatePaymentOrderAllocationDto> orderAllocations;
}
