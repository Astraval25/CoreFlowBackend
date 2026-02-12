package com.astraval.coreflow.modules.subscription.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCapabilitiesResponse {

    private SubscriptionInfo subscription;
    private Map<String, Boolean> features;
    private Map<String, LimitInfo> limits;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String planCode;
        private String status;
        private LocalDateTime endAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LimitInfo {
        private Integer used;
        private Integer limit;
        private Integer remaining;
    }
}
