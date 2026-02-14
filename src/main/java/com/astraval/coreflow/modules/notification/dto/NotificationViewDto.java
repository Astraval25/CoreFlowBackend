package com.astraval.coreflow.modules.notification.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationViewDto {
    private Long notificationId;
    private Long fromCompanyId;
    private Long toCompanyId;
    private String title;
    private String message;
    private String type;
    private String actionLabel;
    private String actionUrl;
    private Boolean isRead;
    private LocalDateTime readDt;
    private LocalDateTime createdDt;
}
