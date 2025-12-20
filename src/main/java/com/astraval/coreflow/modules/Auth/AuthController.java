package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.Auth.dto.LoginRequest;
import com.astraval.coreflow.modules.Auth.dto.LoginResponse;

import org.springframework.beans.factory.annotation.Autowired;
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
            return ApiResponseFactory.accepted(response, "Login successful");
        } catch (Exception e) {
            return ApiResponseFactory.UnauthorizedAccess("Invalid credentials");
        }
    }
}