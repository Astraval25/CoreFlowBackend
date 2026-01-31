package com.astraval.coreflow.modules.payments.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.payments.dto.CreateSellerPayment;
import com.astraval.coreflow.modules.payments.service.SellerPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class SellerPaymentController {

    @Autowired
    private SellerPaymentService sellerPaymentService;

    @PostMapping("/{companyId}/seller-payments")
    public ApiResponse<Map<String, Long>> createSellerPayment(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateSellerPayment request) {
        try {
            Long paymentId = sellerPaymentService.createSellerPayment(companyId, request);
            return ApiResponseFactory.created(
                    Map.of("paymentId", paymentId),
                    "Payment created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}