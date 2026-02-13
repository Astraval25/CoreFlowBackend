package com.astraval.coreflow.modules.notification;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.modules.notification.dto.FcmSendResultDto;
import com.astraval.coreflow.modules.notification.dto.SendFcmNotificationRequest;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FcmNotificationService {

    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    public FcmNotificationService(
            FcmDeviceTokenRepository fcmDeviceTokenRepository,
            UserRepository userRepository,
            SecurityUtil securityUtil,
            ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        this.fcmDeviceTokenRepository = fcmDeviceTokenRepository;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.firebaseMessagingProvider = firebaseMessagingProvider;
    }

    @Transactional
    public void registerCurrentUserToken(String fcmToken) {
        User currentUser = getCurrentUser();

        Optional<FcmDeviceToken> existingTokenOpt = fcmDeviceTokenRepository.findByFcmToken(fcmToken);
        if (existingTokenOpt.isPresent()) {
            FcmDeviceToken existingToken = existingTokenOpt.get();
            existingToken.setUser(currentUser);
            existingToken.setIsActive(true);
            existingToken.setPlatform("ANDROID");
            fcmDeviceTokenRepository.save(existingToken);
            return;
        }

        FcmDeviceToken fcmDeviceToken = new FcmDeviceToken();
        fcmDeviceToken.setUser(currentUser);
        fcmDeviceToken.setFcmToken(fcmToken);
        fcmDeviceToken.setPlatform("ANDROID");
        fcmDeviceToken.setIsActive(true);
        fcmDeviceTokenRepository.save(fcmDeviceToken);
    }

    @Transactional
    public boolean removeCurrentUserToken(String fcmToken) {
        Long userId = getCurrentUserId();
        Optional<FcmDeviceToken> existingTokenOpt = fcmDeviceTokenRepository
                .findByUserUserIdAndFcmTokenAndIsActiveTrue(userId, fcmToken);

        if (existingTokenOpt.isEmpty()) {
            return false;
        }

        FcmDeviceToken existingToken = existingTokenOpt.get();
        existingToken.setIsActive(false);
        fcmDeviceTokenRepository.save(existingToken);
        return true;
    }

    public FcmSendResultDto sendNotification(SendFcmNotificationRequest request) {
        FirebaseMessaging firebaseMessaging = getFirebaseMessaging();
        List<String> targetTokens = resolveTargetTokens(request.getUserIds(), request.getFcmTokens());

        if (targetTokens.isEmpty()) {
            throw new IllegalArgumentException("No active FCM tokens found for provided targets");
        }

        List<String> failedTokens = new ArrayList<>();
        int successCount = 0;

        for (String token : targetTokens) {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(request.getTitle())
                            .setBody(request.getBody())
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build());

            Map<String, String> data = request.getData();
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            try {
                firebaseMessaging.send(messageBuilder.build());
                successCount++;
            } catch (FirebaseMessagingException exception) {
                failedTokens.add(token);
                deactivateTokenIfRequired(token, exception);
                log.warn("Failed to send FCM notification to token: {}", token, exception);
            }
        }

        int totalTargets = targetTokens.size();
        int failureCount = totalTargets - successCount;

        return new FcmSendResultDto(totalTargets, successCount, failureCount, failedTokens);
    }

    private FirebaseMessaging getFirebaseMessaging() {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            throw new IllegalStateException("Firebase is not configured. Enable firebase.enabled and set credentials.");
        }
        return firebaseMessaging;
    }

    private List<String> resolveTargetTokens(List<Long> userIds, List<String> directTokens) {
        LinkedHashSet<String> tokenSet = new LinkedHashSet<>();

        if (userIds != null && !userIds.isEmpty()) {
            tokenSet.addAll(fcmDeviceTokenRepository.findActiveTokensByUserIds(userIds));
        }

        if (directTokens != null && !directTokens.isEmpty()) {
            tokenSet.addAll(directTokens);
        }

        return new ArrayList<>(tokenSet);
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    private Long getCurrentUserId() {
        String currentSubject = securityUtil.getCurrentSub();
        try {
            return Long.parseLong(currentSubject);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Invalid authenticated user id");
        }
    }

    private void deactivateTokenIfRequired(String token, FirebaseMessagingException exception) {
        if (!isUnregisteredToken(exception)) {
            return;
        }

        fcmDeviceTokenRepository.findByFcmToken(token).ifPresent(existingToken -> {
            existingToken.setIsActive(false);
            fcmDeviceTokenRepository.save(existingToken);
        });
    }

    private boolean isUnregisteredToken(FirebaseMessagingException exception) {
        MessagingErrorCode messagingErrorCode = exception.getMessagingErrorCode();
        return MessagingErrorCode.UNREGISTERED.equals(messagingErrorCode);
    }
}
