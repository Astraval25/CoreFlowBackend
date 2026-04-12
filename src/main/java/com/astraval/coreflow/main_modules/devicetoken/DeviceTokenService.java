package com.astraval.coreflow.main_modules.devicetoken;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.devicetoken.dto.RegisterDeviceTokenRequest;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;

@Service
public class DeviceTokenService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void registerToken(Long userId, RegisterDeviceTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Optional<DeviceToken> existing = deviceTokenRepository.findByToken(request.getToken());

        if (existing.isPresent()) {
            DeviceToken deviceToken = existing.get();
            deviceToken.setUser(user);
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setIsActive(true);
            deviceTokenRepository.save(deviceToken);
        } else {
            DeviceToken deviceToken = new DeviceToken();
            deviceToken.setUser(user);
            deviceToken.setToken(request.getToken());
            deviceToken.setDeviceType(request.getDeviceType());
            deviceToken.setIsActive(true);
            deviceTokenRepository.save(deviceToken);
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
        return deviceTokenRepository.findByUserUserIdInAndIsActiveTrue(userIds)
                .stream()
                .map(DeviceToken::getToken)
                .toList();
    }
}
