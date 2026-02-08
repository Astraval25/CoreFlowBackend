package com.astraval.coreflow.modules.payments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.payments.dto.PaymentProofUploadResponse;
import com.astraval.coreflow.modules.payments.service.PaymentProofService;

@RestController
@RequestMapping("/api/companies")
public class PaymentProofController {

    @Autowired
    private PaymentProofService paymentProofService;

    @PostMapping(value = "/{companyId}/payments/payment-proof", consumes = {"multipart/form-data"})
    public ApiResponse<PaymentProofUploadResponse> uploadPaymentProof(
            @PathVariable Long companyId,
            @RequestParam("file") MultipartFile file) {
        try {
            PaymentProofUploadResponse response = paymentProofService.uploadProof(companyId, file);
            return ApiResponseFactory.created(response, "Payment proof uploaded successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
