package com.astraval.coreflow.controller;

import com.astraval.coreflow.dto.request.LoginRequest;
import com.astraval.coreflow.dto.request.RefreshTokenRequest;
import com.astraval.coreflow.dto.request.RegisterRequest;
import com.astraval.coreflow.dto.response.ApiResponse;
import com.astraval.coreflow.dto.response.LoginResponse;
import com.astraval.coreflow.dto.response.RegisterResponse;
import com.astraval.coreflow.service.AuthService;
import com.astraval.coreflow.util.ApiResponseFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponseFactory.accepted(response, "Login Success");
        } catch (Exception e) {
            return ApiResponseFactory.UnauthorizedAccess( "Invalid credentials");
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }
    
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authService.register(request);
            return ApiResponseFactory.accepted(response, "Registration successful");
        } catch (Exception e) {
            return ApiResponseFactory.badRequest(e.getMessage());
        }
    }
}