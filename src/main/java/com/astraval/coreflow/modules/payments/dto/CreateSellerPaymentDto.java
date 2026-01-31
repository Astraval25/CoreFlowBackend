package com.astraval.coreflow.modules.payments.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSellerPaymentDto {

    @NotNull(message = "Customer id is required")
    private Long customerId;
    
    @NotNull(message = "Payment details are required")
    @Valid
    private CreatePaymentDto paymentDetails;
}