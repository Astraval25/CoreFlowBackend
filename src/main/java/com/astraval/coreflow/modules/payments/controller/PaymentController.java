package com.astraval.coreflow.modules.payments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.payments.PaymentStatus;
import com.astraval.coreflow.modules.payments.dto.PaymentViewDto;
import com.astraval.coreflow.modules.payments.service.PaymentService;


@RestController
@RequestMapping("/api/companies")
public class PaymentController {
  @Autowired
  private PaymentService PaymentService;
    
    @GetMapping("/{companyId}/payments/{paymentId}")
    public ApiResponse<PaymentViewDto> getPaymentById(
            @PathVariable Long companyId,
            @PathVariable Long paymentId) {
        try {
            PaymentViewDto payment = PaymentService.getPaymentViewById(paymentId);
            return ApiResponseFactory.accepted(payment, "Payment details retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/payments/{paymentId}/paid")
    public ApiResponse<String> markPaymentPaid(@PathVariable Long companyId, @PathVariable Long paymentId) {
        try {
            PaymentService.updatePaymentStatus(companyId, paymentId, PaymentStatus.getPaid());
            return ApiResponseFactory.accepted("Payment marked as paid.", "Payment marked as paid.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/payments/{paymentId}/viewed")
    public ApiResponse<String> markPaymentViewed(@PathVariable Long companyId, @PathVariable Long paymentId) {
        try {
            PaymentService.updatePaymentStatus(companyId, paymentId, PaymentStatus.getPaymentViewed());
            return ApiResponseFactory.accepted("Payment marked as viewed.", "Payment marked as viewed.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/payments/{paymentId}/refund")
    public ApiResponse<String> markPaymentRefund(@PathVariable Long companyId, @PathVariable Long paymentId) {
        try {
            PaymentService.updatePaymentStatus(companyId, paymentId, PaymentStatus.getPaymentRefund());
            return ApiResponseFactory.accepted("Payment marked as refund.", "Payment marked as refund.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/payments/{paymentId}/failed")
    public ApiResponse<String> markPaymentFailed(@PathVariable Long companyId, @PathVariable Long paymentId) {
        try {
            PaymentService.updatePaymentStatus(companyId, paymentId, PaymentStatus.getPaymentFailed());
            return ApiResponseFactory.accepted("Payment marked as failed.", "Payment marked as failed.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/{companyId}/payments/{paymentId}/partially-paid")
    public ApiResponse<String> markPaymentPartiallyPaid(@PathVariable Long companyId, @PathVariable Long paymentId) {
        try {
            PaymentService.updatePaymentStatus(companyId, paymentId, PaymentStatus.getPartiallyPaid());
            return ApiResponseFactory.accepted("Payment marked as partially paid.", "Payment marked as partially paid.");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
