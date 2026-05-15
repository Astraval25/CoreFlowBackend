package com.astraval.coreflow.main_modules.notification.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class NotificationPageDto {
    private List<NotificationViewDto> notifications;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private long totalUnreadCount;
    private Map<String, Long> unreadCountByEntity;
}
