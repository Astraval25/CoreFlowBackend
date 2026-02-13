package com.astraval.coreflow.modules.notification.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SendFcmNotificationRequest {

    @NotBlank(message = "Notification title is required")
    private String title;

    @NotBlank(message = "Notification body is required")
    private String body;

    private List<@Positive(message = "User IDs must be positive") Long> userIds;
    private List<@NotBlank(message = "FCM tokens must not be blank") String> fcmTokens;
    private Map<String, String> data;

    @AssertTrue(message = "At least one target is required (userIds or fcmTokens)")
    public boolean isTargetProvided() {
        boolean hasUserIds = userIds != null && !userIds.isEmpty();
        boolean hasTokens = fcmTokens != null && !fcmTokens.isEmpty();
        return hasUserIds || hasTokens;
    }
}
