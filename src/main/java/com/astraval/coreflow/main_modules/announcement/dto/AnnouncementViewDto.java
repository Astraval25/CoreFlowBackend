package com.astraval.coreflow.main_modules.announcement.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AnnouncementViewDto {
    private Long announcementId;
    private String announcementKey;
    private String title;
    private String message;
    private String actionLabel;
    private String actionUrl;
    private LocalDateTime createdDt;
}
