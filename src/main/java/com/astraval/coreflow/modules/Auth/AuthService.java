package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.exception.InvalidCredentialsException;
import com.astraval.coreflow.common.util.JwtUtil;
import com.astraval.coreflow.modules.Auth.dto.LoginRequest;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserService;
import com.astraval.coreflow.modules.user.dto.LoginResponse;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.modules.userrolemap.UserRoleMap;
import com.astraval.coreflow.modules.userrolemap.UserRoleMapRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;

    public LoginResponse login(@Valid LoginRequest request) {
        Optional<User> userOpt = userService.runSelect(request.getEmail());
        
        // Check the user name is correct 
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid credentials...");
        }
        User user = userOpt.get();
        
        // check the password is correct
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials..");
        }
        
        // Get user role and companies
        String roleCode = getUserRole(user);
        List<Long> companyIds = getUserCompanyIds(user);
        
        // Generate tokens
        String token = jwtUtil.generateToken(user.getUserId(), roleCode, companyIds, 
            user.getDefaultCompany().getCompanyId(), user.getDefaultCompany().getCompanyName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        
        return new LoginResponse(
            token,
            refreshToken,
            user.getUserId().intValue(),
            roleCode,
            getRoleLandingUrl(user),
            user.getDefaultCompany().getCompanyId(),
            user.getDefaultCompany().getCompanyName(),
            companyIds
        );
    }
    
    private String getUserRole(User user) {
        List<UserRoleMap> userRoles = userRoleMapRepository.findActiveRolesByUserId(user.getUserId().intValue());
        if (userRoles.isEmpty()) {
            throw new InvalidCredentialsException("No active role found for user");
        }
        return userRoles.get(0).getRole().getRoleCode();
    }
    
    private List<Long> getUserCompanyIds(User user) {
        return user.getCompanyMapping().stream()
            .filter(mapping -> mapping.getIsActive())
            .map(UserCompanyMap::getCompany)
            .map(company -> company.getCompanyId().longValue())
            .toList();
    }
    
    private String getRoleLandingUrl(User user) {
        List<UserRoleMap> userRoles = userRoleMapRepository.findActiveRolesByUserId(user.getUserId().intValue());
        if (userRoles.isEmpty()) {
            throw new InvalidCredentialsException("No active role found for user");
        }
        return userRoles.get(0).getRole().getLandingUrl();
    }
}