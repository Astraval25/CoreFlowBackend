package com.astraval.coreflow.main_modules.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.main_modules.user.dto.UpdateUserProfileDto;
import com.astraval.coreflow.main_modules.user.dto.UserProfileDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDto> getMyProfile() {
        try {
            UserProfileDto profile = userService.getCurrentUserProfile();
            return ApiResponseFactory.accepted(profile, "Profile retrieved successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileDto> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileDto request) {
        try {
            UserProfileDto profile = userService.updateCurrentUserProfile(request);
            return ApiResponseFactory.updated(profile, "Profile updated successfully");
        } catch (RuntimeException e) {
            return ApiResponseFactory.error(e.getMessage(), 406);
        }
    }
}
