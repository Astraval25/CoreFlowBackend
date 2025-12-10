package com.astraval.coreflow.modules.auth;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import com.astraval.coreflow.global.util.RefreshTokenRequest;
import com.astraval.coreflow.modules.auth.dto.LoginRequest;
import com.astraval.coreflow.modules.auth.dto.LoginResponse;
import com.astraval.coreflow.modules.auth.dto.RegisterRequest;
import com.astraval.coreflow.modules.auth.dto.RegisterResponse;

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