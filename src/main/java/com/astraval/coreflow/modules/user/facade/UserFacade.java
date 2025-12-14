package com.astraval.coreflow.modules.user.facade;

import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.dto.LoginRequest;
import com.astraval.coreflow.modules.user.dto.LoginResponse;
import com.astraval.coreflow.modules.user.dto.RegisterRequest;
import com.astraval.coreflow.modules.user.dto.RegisterResponse;

public interface UserFacade {
    User getUserById(Integer userId);
    User getUserByEmail(String email);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    RegisterResponse register(RegisterRequest request);
}