package com.astraval.coreflow.main_modules.announcement;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.main_modules.announcement.dto.AnnouncementViewDto;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/current")
    public ApiResponse<AnnouncementViewDto> getCurrentAnnouncement() {
        try {
            AnnouncementViewDto announcement = announcementService.getCurrentAnnouncement().orElse(null);
            return ApiResponseFactory.accepted(announcement, "Current announcement retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PostMapping("/{announcementId}/dismiss")
    public ApiResponse<Void> dismissAnnouncement(@PathVariable Long announcementId) {
        try {
            announcementService.dismissAnnouncement(announcementId);
            return ApiResponseFactory.updated(null, "Announcement dismissed successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
