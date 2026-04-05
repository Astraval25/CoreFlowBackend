package com.astraval.coreflow.main_modules.Auth.dto;

import lombok.Data;

@Data
public class RegisterResponse {
    private String email;
    private String landingUrl = "/verify/user";
}