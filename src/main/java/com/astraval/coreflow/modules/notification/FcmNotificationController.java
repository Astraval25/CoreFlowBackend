package com.astraval.coreflow.modules.notification;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.notification.dto.FcmSendResultDto;
import com.astraval.coreflow.modules.notification.dto.RegisterFcmTokenRequest;
import com.astraval.coreflow.modules.notification.dto.RemoveFcmTokenRequest;
import com.astraval.coreflow.modules.notification.dto.SendFcmNotificationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications/fcm")
public class FcmNotificationController {

    private final FcmNotificationService fcmNotificationService;

    public FcmNotificationController(FcmNotificationService fcmNotificationService) {
        this.fcmNotificationService = fcmNotificationService;
    }

    @PostMapping("/device-token")
    public ApiResponse<String> registerDeviceToken(@Valid @RequestBody RegisterFcmTokenRequest request) {
        fcmNotificationService.registerCurrentUserToken(request.getFcmToken());
        return ApiResponseFactory.created(request.getFcmToken(), "FCM device token registered");
    }

    @DeleteMapping("/device-token")
    public ApiResponse<String> removeDeviceToken(@Valid @RequestBody RemoveFcmTokenRequest request) {
        boolean removed = fcmNotificationService.removeCurrentUserToken(request.getFcmToken());
        if (!removed) {
            return ApiResponseFactory.badRequest("FCM token not found for current user");
        }
        return ApiResponseFactory.deleted("FCM device token removed");
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADM')")
    public ApiResponse<FcmSendResultDto> sendNotification(@Valid @RequestBody SendFcmNotificationRequest request) {
        FcmSendResultDto result = fcmNotificationService.sendNotification(request);
        return ApiResponseFactory.accepted(result, "FCM notification processed");
    }
}
