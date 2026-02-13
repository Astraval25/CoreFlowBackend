package com.astraval.coreflow.modules.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationOpenResponse {
    private Long notificationId;
    private String actionLabel;
    private String actionUrl;
}
