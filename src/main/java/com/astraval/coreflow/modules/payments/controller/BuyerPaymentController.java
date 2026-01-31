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
import com.astraval.coreflow.modules.payments.dto.CreateBuyerPaymentDto;
import com.astraval.coreflow.modules.payments.service.BuyerPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class BuyerPaymentController {

    @Autowired
    private BuyerPaymentService buyerPaymentService;

    @PostMapping("/{companyId}/buyer-payments")
    public ApiResponse<Map<String, Long>> createBuyerPayment(
            @PathVariable Long companyId,
            @Valid @RequestBody CreateBuyerPaymentDto request) {
        try {
            Long paymentId = buyerPaymentService.createBuyerPayment(companyId, request);
            return ApiResponseFactory.created(
                    Map.of("paymentId", paymentId),
                    "Payment created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}