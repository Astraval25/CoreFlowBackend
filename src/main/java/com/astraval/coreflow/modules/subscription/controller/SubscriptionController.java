package com.astraval.coreflow.modules.subscription.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.modules.subscription.dto.SubscriptionCapabilitiesResponse;
import com.astraval.coreflow.modules.subscription.service.SubscriptionService;
import com.astraval.coreflow.modules.subscription.service.SubscriptionWebhookService;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionWebhookService subscriptionWebhookService;
    private final SecurityUtil securityUtil;

    public SubscriptionController(SubscriptionService subscriptionService,
            SubscriptionWebhookService subscriptionWebhookService,
            SecurityUtil securityUtil) {
        this.subscriptionService = subscriptionService;
        this.subscriptionWebhookService = subscriptionWebhookService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/me/capabilities")
    public ApiResponse<SubscriptionCapabilitiesResponse> getMyCapabilities() {
        Long companyId = securityUtil.getCurrentCompanyIdAsLong();
        if (companyId == null) {
            return ApiResponseFactory.error("Unable to resolve current company", 406);
        }

        SubscriptionCapabilitiesResponse response = subscriptionService.getCapabilities(companyId);
        return ApiResponseFactory.accepted(response, "Subscription capabilities retrieved successfully");
    }

    @PostMapping("/webhook/razorpay")
    public ApiResponse<String> processRazorpayWebhook(
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload) {
        subscriptionWebhookService.processRazorpayWebhook(signature, payload);
        return ApiResponseFactory.accepted("ok", "Webhook processed");
    }
}
