package com.astraval.coreflow.main_modules.companyref;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.main_modules.companyref.dto.CompanyOrderRefDto;
import com.astraval.coreflow.main_modules.companyref.dto.CompanyPaymentRefDto;

@RestController
@RequestMapping("/api/companies/{companyId}")
public class CompanyRefController {

    @Autowired
    private CompanyRefService companyRefService;

    @GetMapping("/orders/{orderId}/ref")
    public ResponseEntity<ApiResponse<CompanyOrderRefDto>> getOrderRef(
            @PathVariable Long companyId, @PathVariable Long orderId) {
        CompanyOrderRef ref = companyRefService.getOrderRef(companyId, orderId)
            .orElseThrow(() -> new RuntimeException("Order ref not found"));

        CompanyOrderRefDto dto = new CompanyOrderRefDto(
            ref.getCompanyOrderRefId(), ref.getLocalOrderNumber(),
            ref.getInternalRemarks(), ref.getInternalStatus(),
            ref.getInternalTags(), ref.getCustomReference()
        );
        return ResponseEntity.ok(ApiResponseFactory.ok(dto, "Order ref retrieved"));
    }

    @PutMapping("/orders/{orderId}/ref")
    public ResponseEntity<ApiResponse<String>> updateOrderRef(
            @PathVariable Long companyId, @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {
        companyRefService.updateOrderRef(companyId, orderId,
            body.get("internalRemarks"), body.get("internalStatus"),
            body.get("internalTags"), body.get("customReference"));
        return ResponseEntity.ok(ApiResponseFactory.ok("Updated", "Order ref updated"));
    }

    @GetMapping("/payments/{paymentId}/ref")
    public ResponseEntity<ApiResponse<CompanyPaymentRefDto>> getPaymentRef(
            @PathVariable Long companyId, @PathVariable Long paymentId) {
        CompanyPaymentRef ref = companyRefService.getPaymentRef(companyId, paymentId)
            .orElseThrow(() -> new RuntimeException("Payment ref not found"));

        CompanyPaymentRefDto dto = new CompanyPaymentRefDto(
            ref.getCompanyPaymentRefId(), ref.getLocalPaymentNumber(),
            ref.getInternalRemarks(), ref.getInternalStatus(),
            ref.getCustomReference()
        );
        return ResponseEntity.ok(ApiResponseFactory.ok(dto, "Payment ref retrieved"));
    }

    @PutMapping("/payments/{paymentId}/ref")
    public ResponseEntity<ApiResponse<String>> updatePaymentRef(
            @PathVariable Long companyId, @PathVariable Long paymentId,
            @RequestBody Map<String, String> body) {
        companyRefService.updatePaymentRef(companyId, paymentId,
            body.get("internalRemarks"), body.get("internalStatus"),
            body.get("customReference"));
        return ResponseEntity.ok(ApiResponseFactory.ok("Updated", "Payment ref updated"));
    }
}
