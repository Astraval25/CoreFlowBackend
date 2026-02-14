package com.astraval.coreflow.modules.notification.dto;

import java.util.List;

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
}
