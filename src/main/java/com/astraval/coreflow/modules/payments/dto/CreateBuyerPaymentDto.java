package com.astraval.coreflow.modules.payments.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBuyerPaymentDto {

    @NotNull(message = "Vendor id is required")
    private Long vendorId;
    
    @NotNull(message = "Payment details are required")
    @Valid
    private CreatePaymentDto paymentDetails;
}