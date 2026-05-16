package com.astraval.coreflow.main_modules.devicetoken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMapRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;

@Service
public class FirebaseMessagingService {

    private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingService.class);

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;

    @Async
    public void sendToCompanyUsers(Long companyId, String title, String body, Map<String, String> data) {
        try {
            List<Long> userIds = userCompanyMapRepository.findUserIdsByCompanyId(companyId);

            if (userIds.isEmpty()) {
                return;
            }

            sendToUsers(userIds, title, body, data);
        } catch (Exception e) {
            log.error("Failed to send push to company {}: {}", companyId, e.getMessage());
        }
    }

    public void sendToUsers(List<Long> userIds, String title, String body, Map<String, String> data) {
        List<DeviceToken> activeDeviceTokens = deviceTokenRepository.findByUserUserIdInAndIsActiveTrue(userIds);

        if (activeDeviceTokens.isEmpty()) {
            log.warn("No active FCM tokens found for users: {}", userIds);
            return;
        }

        try {
            Map<String, String> safeData = data == null ? Map.of()
                    : data.entrySet().stream()
                            .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            int badgeCount = parseBadgeCount(safeData);

            List<String> androidTokens = activeDeviceTokens.stream()
                    .filter(this::isAndroidToken)
                    .map(DeviceToken::getToken)
                    .toList();
            List<String> notificationTokens = activeDeviceTokens.stream()
                    .filter(deviceToken -> !isAndroidToken(deviceToken))
                    .map(DeviceToken::getToken)
                    .toList();

            int totalSuccess = 0;
            int totalFailure = 0;

            if (!androidTokens.isEmpty()) {
                BatchResponse androidResponse = sendAndroidDataMessage(androidTokens, title, body, safeData);
                totalSuccess += androidResponse.getSuccessCount();
                totalFailure += androidResponse.getFailureCount();
                if (androidResponse.getFailureCount() > 0) {
                    handleFailedTokens(androidTokens, androidResponse.getResponses());
                }
            }

            if (!notificationTokens.isEmpty()) {
                BatchResponse notificationResponse = sendNotificationMessage(
                        notificationTokens, title, body, safeData, badgeCount);
                totalSuccess += notificationResponse.getSuccessCount();
                totalFailure += notificationResponse.getFailureCount();
                if (notificationResponse.getFailureCount() > 0) {
                    handleFailedTokens(notificationTokens, notificationResponse.getResponses());
                }
            }

            log.info("FCM sent: {} success, {} failed", totalSuccess, totalFailure);
        } catch (Exception e) {
            log.error("FCM send failed (title='{}', users={}, tokens={}): {}", title, userIds.size(),
                    activeDeviceTokens.size(), e.getMessage(), e);
        }
    }

    private BatchResponse sendAndroidDataMessage(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> safeData) throws Exception {
        Map<String, String> androidData = new HashMap<>(safeData);
        androidData.putIfAbsent("title", title == null ? "" : title);
        androidData.putIfAbsent("body", body == null ? "" : body);

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .putAllData(androidData)
                .build();

        return FirebaseMessaging.getInstance().sendEachForMulticast(message);
    }

    private BatchResponse sendNotificationMessage(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> safeData,
            int badgeCount) throws Exception {
        AndroidNotification.Builder androidNotification = AndroidNotification.builder()
                .setChannelId("coreflow_notifications_v2")
                .setSound("default");
        if (badgeCount >= 0) {
            androidNotification.setNotificationCount(badgeCount);
        }

        Aps.Builder aps = Aps.builder()
                .setSound("default");
        if (badgeCount >= 0) {
            aps.setBadge(badgeCount);
        }

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(androidNotification.build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(aps.build())
                        .build())
                .putAllData(safeData)
                .build();

        return FirebaseMessaging.getInstance().sendEachForMulticast(message);
    }

    private boolean isAndroidToken(DeviceToken deviceToken) {
        return "ANDROID".equalsIgnoreCase(deviceToken.getDeviceType());
    }

    private void handleFailedTokens(List<String> tokens, List<SendResponse> responses) {
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                String errorCode = responses.get(i).getException() != null
                        ? responses.get(i).getException().getMessagingErrorCode() != null
                                ? responses.get(i).getException().getMessagingErrorCode().name()
                                : "UNKNOWN"
                        : "UNKNOWN";

                if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
                    String failedToken = tokens.get(i);
                    deviceTokenRepository.findByToken(failedToken).ifPresent(dt -> {
                        dt.setIsActive(false);
                        deviceTokenRepository.save(dt);
                        log.info("Deactivated stale FCM token: {}...", failedToken.substring(0, 20));
                    });
                }
            }
        }
    }

    private int parseBadgeCount(Map<String, String> data) {
        for (String key : List.of("badge", "badgeCount", "unreadCount", "notificationCount")) {
            String value = data.get(key);
            if (value == null || value.isBlank()) {
                continue;
            }

            try {
                return Math.max(0, Integer.parseInt(value.trim()));
            } catch (NumberFormatException ignored) {
                // Keep looking for another badge key.
            }
        }

        return -1;
    }
}
