package com.astraval.coreflow.main_modules.devicetoken;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMapRepository;
import com.google.firebase.messaging.BatchResponse;
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
            List<Long> userIds = userCompanyMapRepository
                    .findByCompanyCompanyIdAndIsActiveTrue(companyId)
                    .stream()
                    .map(ucm -> ucm.getUser().getUserId())
                    .toList();

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
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            if (response.getFailureCount() > 0) {
                handleFailedTokens(tokens, response.getResponses());
            }

            log.info("FCM sent: {} success, {} failed",
                    response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            log.error("FCM send failed: {}", e.getMessage());
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
