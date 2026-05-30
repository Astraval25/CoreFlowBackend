package com.astraval.coreflow.main_modules.user.dto;

public record UserProfileDto(
        Long userId,
        String userName,
        String firstName,
        String lastName,
        String email,
        String contactNo) {
}
