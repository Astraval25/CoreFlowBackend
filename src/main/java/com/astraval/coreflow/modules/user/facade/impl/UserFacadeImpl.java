package com.astraval.coreflow.modules.user.facade.impl;

import com.astraval.coreflow.modules.user.facade.UserFacade;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import com.astraval.coreflow.modules.user.AuthService;
import com.astraval.coreflow.modules.user.dto.LoginRequest;
import com.astraval.coreflow.modules.user.dto.LoginResponse;
import com.astraval.coreflow.modules.user.dto.RegisterRequest;
import com.astraval.coreflow.modules.user.dto.RegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserFacadeImpl implements UserFacade {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;

    @Override
    public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email);
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        return authService.login(request);
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        return authService.refreshToken(refreshToken);
    }
    
    @Override
    public RegisterResponse register(RegisterRequest request) {
        return authService.register(request);
    }
}