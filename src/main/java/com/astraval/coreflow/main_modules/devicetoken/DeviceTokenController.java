package com.astraval.coreflow.main_modules.devicetoken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.common.util.SecurityUtil;
import com.astraval.coreflow.main_modules.devicetoken.dto.RegisterDeviceTokenRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

    @Autowired
    private DeviceTokenService deviceTokenService;

    @Autowired
    private SecurityUtil securityUtil;

    @PostMapping
    public ApiResponse<String> registerToken(@Valid @RequestBody RegisterDeviceTokenRequest request) {
        try {
            Long userId = Long.parseLong(securityUtil.getCurrentSub());
            deviceTokenService.registerToken(userId, request);
            return ApiResponseFactory.created(null, "Device token registered successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @DeleteMapping
    public ApiResponse<String> deregisterToken(@RequestParam String token) {
        try {
            if (token.isBlank()) {
                return ApiResponseFactory.badRequest("Token is required");
            }
            deviceTokenService.deregisterToken(token);
            return ApiResponseFactory.deleted("Device token deregistered successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
