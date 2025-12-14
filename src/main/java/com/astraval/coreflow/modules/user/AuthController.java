package com.astraval.coreflow.modules.user;

import com.astraval.coreflow.global.util.ApiResponse;
import com.astraval.coreflow.global.util.ApiResponseFactory;
import com.astraval.coreflow.global.util.RefreshTokenRequest;
import com.astraval.coreflow.modules.user.dto.LoginRequest;
import com.astraval.coreflow.modules.user.dto.LoginResponse;
import com.astraval.coreflow.modules.user.dto.RegisterRequest;
import com.astraval.coreflow.modules.user.dto.RegisterResponse;
import com.astraval.coreflow.modules.user.facade.UserFacade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserFacade userFacade;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userFacade.login(request);
            return ApiResponseFactory.accepted(response, "Login Success");
        } catch (Exception e) {
            return ApiResponseFactory.UnauthorizedAccess( "Invalid credentials");
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = userFacade.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid refresh token");
        }
    }
    
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = userFacade.register(request);
            return ApiResponseFactory.accepted(response, "Registration successful");
        } catch (Exception e) {
            return ApiResponseFactory.badRequest(e.getMessage());
        }
    }
}