package com.astraval.coreflow.modules.payments.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProofUploadResponse {
    private String fsId;
    private String transactionId;
    private Double amount;
    private String extractedText;
}
