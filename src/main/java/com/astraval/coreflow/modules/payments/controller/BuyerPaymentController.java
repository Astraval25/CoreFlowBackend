package com.astraval.coreflow.modules.payments.controller;

import java.util.List;
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
import com.astraval.coreflow.modules.payments.dto.PayerPaymentSummaryDto;
import com.astraval.coreflow.modules.payments.dto.UpdateBuyerPaymentDto;
import com.astraval.coreflow.modules.payments.service.BuyerPaymentService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;


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

    @GetMapping("/{companyId}/payer/payments")
    public ApiResponse<List<PayerPaymentSummaryDto>> getPaymentSummaryByCompanyId(@PathVariable Long companyId) {
        try {
            List<PayerPaymentSummaryDto> payments = buyerPaymentService.getPayerPaymentSummaryByCompanyId(companyId);
            return ApiResponseFactory.accepted(payments, "Payment summary retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @PutMapping("/{companyId}/buyer-payments/{paymentId}")
    public ApiResponse<String> updateBuyerPayment(
            @PathVariable Long companyId,
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdateBuyerPaymentDto request) {
        try {
            buyerPaymentService.updateBuyerPayment(companyId, paymentId, request);
            return ApiResponseFactory.accepted("Payment updated successfully", "Payment updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
    
    @DeleteMapping("/{companyId}/buyer-payments/{paymentId}/allocations/{allocationId}")
    public ApiResponse<String> deletePaymentOrderAllocation(
            @PathVariable Long companyId,
            @PathVariable Long paymentId,
            @PathVariable Long allocationId) {
        try {
            buyerPaymentService.deletePaymentOrderAllocation(companyId, paymentId, allocationId);
            return ApiResponseFactory.accepted("Payment allocation deleted successfully", "Payment allocation deleted successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
  
}