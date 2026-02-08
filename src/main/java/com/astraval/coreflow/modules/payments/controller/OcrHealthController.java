package com.astraval.coreflow.modules.payments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.payments.dto.OcrHealthResponse;
import com.astraval.coreflow.modules.payments.service.PaymentProofTextExtractor;

@RestController
@RequestMapping("/api/health")
public class OcrHealthController {

    @Autowired
    private PaymentProofTextExtractor paymentProofTextExtractor;

    @GetMapping("/ocr")
    public ApiResponse<OcrHealthResponse> getOcrHealth() {
        String version = paymentProofTextExtractor.getTesseractVersion();
        if (version == null) {
            return ApiResponseFactory.accepted(
                    new OcrHealthResponse(false, null, "Tesseract not found or not executable"),
                    "OCR health check");
        }
        return ApiResponseFactory.accepted(
                new OcrHealthResponse(true, version, null),
                "OCR health check");
    }
}
