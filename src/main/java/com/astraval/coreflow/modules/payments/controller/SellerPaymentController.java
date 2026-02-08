package com.astraval.coreflow.modules.payments.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.payments.dto.CreateSellerPaymentDto;
import com.astraval.coreflow.modules.payments.dto.SellerPaymentSummaryDto;
import com.astraval.coreflow.modules.payments.dto.UpdateSellerPaymentDto;
import com.astraval.coreflow.modules.payments.service.SellerPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class SellerPaymentController {

    @Autowired
    private SellerPaymentService sellerPaymentService;

    @PostMapping("/{companyId}/payments-received")
    public ApiResponse<Map<String, Long>> createSellerPayment(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateSellerPaymentDto request) {
        try {
            Long paymentId = sellerPaymentService.createSellerPayment(companyId, request);
            return ApiResponseFactory.created(
                    Map.of("paymentId", paymentId),
                    "Payment created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/{companyId}/payments-received/summary")
    public ApiResponse<List<SellerPaymentSummaryDto>> getPaymentSummaryByCompanyId(@PathVariable Long companyId) {
        try {
            List<SellerPaymentSummaryDto> payments = sellerPaymentService.getSellerPaymentSummaryByCompanyId(companyId);
            return ApiResponseFactory.accepted(payments, "Payment summary retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/payments-received/{paymentId}")
    public ApiResponse<String> updateSellerPayment(
            @PathVariable Long companyId,
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdateSellerPaymentDto request) {
        try {
            sellerPaymentService.updateSellerPayment(companyId, paymentId, request);
            return ApiResponseFactory.accepted("Payment updated successfully", "Payment updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PostMapping(value = "/{companyId}/payments-received/{paymentId}/payment-proof", consumes = {"multipart/form-data"})
    public ApiResponse<Map<String, String>> uploadPaymentProof(
            @PathVariable Long companyId,
            @PathVariable Long paymentId,
            @RequestParam("file") MultipartFile file) {
        try {
            String fsId = sellerPaymentService.uploadPaymentProof(companyId, paymentId, file);
            return ApiResponseFactory.created(
                    Map.of("fsId", fsId),
                    "Payment proof uploaded successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
