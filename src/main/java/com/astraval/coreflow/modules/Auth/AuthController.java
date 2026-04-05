package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.util.ApiResponse;
import com.astraval.coreflow.common.util.ApiResponseFactory;
import com.astraval.coreflow.modules.Auth.dto.*;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


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
    public ApiResponse<Object> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Object response = authService.refreshToken(request.getRefreshToken());
        String message = response instanceof EmployeeLoginResponse
                ? "Employee token refreshed successfully"
                : "Token refreshed successfully";
        return ApiResponseFactory.accepted(response, message);
    }

    @PostMapping("/employee/login")
    public ApiResponse<EmployeeLoginResponse> employeeLogin(
            @Valid @RequestBody EmployeeLoginRequest request) {
        EmployeeLoginResponse response = authService.employeeLogin(request);
        return ApiResponseFactory.accepted(response, "Employee login successful");
    }

}
