package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.Auth.dto.*;

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
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponseFactory.accepted(response, "Login successful");
    }
    
    @PostMapping("/register")
    public ApiResponse<RegisterResponse> registerNewUser(@Valid @RequestBody RegisterRequest dto) {
        RegisterResponse response = authService.registerNewUser(dto);
        return ApiResponseFactory.created(response, "Register successful");
    }
    
    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ApiResponseFactory.accepted(response, "Token refreshed successfully");
    }

    @PostMapping("/employee/company/{companyId}")
    public ApiResponse<EmployeeLoginResponse> employeeLogin(
            @PathVariable Long companyId,
            @Valid @RequestBody EmployeeLoginRequest request) {
        EmployeeLoginResponse response = authService.employeeLogin(companyId, request);
        return ApiResponseFactory.accepted(response, "Employee login successful");
    }

    @PostMapping("/employee/refresh-token")
    public ApiResponse<EmployeeLoginResponse> employeeRefreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        EmployeeLoginResponse response = authService.employeeRefreshToken(request.getRefreshToken());
        return ApiResponseFactory.accepted(response, "Employee token refreshed successfully");
    }

}