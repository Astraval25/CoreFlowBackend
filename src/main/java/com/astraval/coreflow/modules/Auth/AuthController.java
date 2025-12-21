package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.Auth.dto.LoginRequest;
import com.astraval.coreflow.modules.Auth.dto.LoginResponse;
import com.astraval.coreflow.modules.Auth.dto.RegisterRequest;
import com.astraval.coreflow.modules.Auth.dto.RegisterResponse;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        // try {
            LoginResponse response = authService.login(request);
            return ApiResponseFactory.accepted(response, "Login successful");
        // } catch (Exception e) {
        //     return ApiResponseFactory.UnauthorizedAccess("Invalid credentials");
        // }
    }
    
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> registerNewUser(@Valid @RequestBody RegisterRequest dto) {
        RegisterResponse response = authService.registerNewUser(dto);
        return ApiResponseFactory.created(response, "Register successful");
    }
    
}