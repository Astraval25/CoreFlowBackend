package com.astraval.coreflow.modules.payments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
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
}
