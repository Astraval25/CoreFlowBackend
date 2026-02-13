package com.astraval.coreflow.modules.notification;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.notification.dto.CreateNotificationRequest;
import com.astraval.coreflow.modules.notification.dto.NotificationOpenResponse;
import com.astraval.coreflow.modules.notification.dto.NotificationPageDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/notifications")
    public ApiResponse<Map<String, Long>> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        try {
            Long notificationId = notificationService.createNotification(request);
            return ApiResponseFactory.created(Map.of("notificationId", notificationId), "Notification created successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/companies/{companyId}/notifications")
    public ApiResponse<NotificationPageDto> getCompanyNotifications(@PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page) {
        try {
            return ApiResponseFactory.accepted(notificationService.getCompanyNotifications(companyId, page),
                    "Notifications retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @GetMapping("/companies/{companyId}/notifications/unread-count")
    public ApiResponse<Map<String, Long>> getUnreadCount(@PathVariable Long companyId) {
        try {
            Long count = notificationService.getCompanyUnreadCount(companyId);
            return ApiResponseFactory.accepted(Map.of("unreadCount", count), "Unread count retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/companies/{companyId}/notifications/{notificationId}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long companyId, @PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(companyId, notificationId);
            return ApiResponseFactory.updated(null, "Notification marked as read");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PatchMapping("/companies/{companyId}/notifications/read-all")
    public ApiResponse<Map<String, Long>> markAllAsRead(@PathVariable Long companyId) {
        try {
            Long updatedCount = notificationService.markAllAsRead(companyId);
            return ApiResponseFactory.updated(Map.of("updatedCount", updatedCount), "All notifications marked as read");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PostMapping("/companies/{companyId}/notifications/{notificationId}/open")
    public ApiResponse<NotificationOpenResponse> openNotification(@PathVariable Long companyId, @PathVariable Long notificationId) {
        try {
            NotificationOpenResponse response = notificationService.openNotification(companyId, notificationId);
            return ApiResponseFactory.accepted(response, "Notification opened successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
