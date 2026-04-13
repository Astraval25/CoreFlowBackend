package com.astraval.coreflow.main_modules.devicetoken;

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
    private DeviceTokenService deviceTokenService;

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
        List<String> tokens = deviceTokenService.getActiveTokensForUsers(userIds);

        if (tokens.isEmpty()) {
            log.warn("No active FCM tokens found for users: {}", userIds);
            return;
        }

        try {
            Map<String, String> safeData = data == null ? Map.of()
                    : data.entrySet().stream()
                            .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId("coreflow_notifications_v2")
                                    .setSound("default")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .putAllData(safeData)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            if (response.getFailureCount() > 0) {
                handleFailedTokens(tokens, response.getResponses());
            }

            log.info("FCM sent: {} success, {} failed",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            log.error("FCM send failed (title='{}', users={}, tokens={}): {}", title, userIds.size(), tokens.size(),
                    e.getMessage(), e);
        }
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
}
