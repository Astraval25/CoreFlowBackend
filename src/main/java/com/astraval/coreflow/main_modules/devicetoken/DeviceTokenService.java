package com.astraval.coreflow.main_modules.devicetoken;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.devicetoken.dto.RegisterDeviceTokenRequest;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;

@Service
public class DeviceTokenService {

    private static final Logger log = LoggerFactory.getLogger(DeviceTokenService.class);

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void registerToken(Long userId, RegisterDeviceTokenRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new RuntimeException("FCM token is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Optional<DeviceToken> existing = deviceTokenRepository.findByToken(request.getToken());

        if (existing.isPresent()) {
            DeviceToken deviceToken = existing.get();
            deviceToken.setUser(user);
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setIsActive(true);
            deviceTokenRepository.save(deviceToken);
            log.info("Reactivated FCM token for userId={} deviceType={}", userId, request.getDeviceType());
        } else {
            DeviceToken deviceToken = new DeviceToken();
            deviceToken.setUser(user);
            deviceToken.setToken(request.getToken());
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setIsActive(true);
            deviceTokenRepository.save(deviceToken);
            log.info("Registered new FCM token for userId={} deviceType={}", userId, request.getDeviceType());
        }
    }

    @Transactional
    public void deregisterToken(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
            deviceToken.setIsActive(false);
            deviceTokenRepository.save(deviceToken);
        });
    }

    public List<String> getActiveTokensForUsers(List<Long> userIds) {
        List<String> tokens = deviceTokenRepository.findByUserUserIdInAndIsActiveTrue(userIds)
                .stream()
                .map(DeviceToken::getToken)
                .toList();
        log.info("Resolved {} active FCM tokens for {} users", tokens.size(), userIds.size());
        return tokens;
    }
}
