package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.exception.InvalidCredentialsException;
import com.astraval.coreflow.common.util.JwtUtil;
import com.astraval.coreflow.common.util.LogUtil;
import com.astraval.coreflow.modules.Auth.dto.LoginRequest;
import com.astraval.coreflow.modules.Auth.dto.LoginResponse;
import com.astraval.coreflow.modules.Auth.dto.RegisterRequest;
import com.astraval.coreflow.modules.Auth.dto.RegisterResponse;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import com.astraval.coreflow.modules.user.UserService;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMapRepository;
import com.astraval.coreflow.modules.userrolemap.UserRoleMap;
import com.astraval.coreflow.modules.userrolemap.UserRoleMapRepository;
import com.astraval.coreflow.modules.otp.OtpService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LogUtil.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapRepository userRoleMapRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyMapRepository userCompanyMapRepository;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository, UserService userService, JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder, UserRoleMapRepository userRoleMapRepository,
            CompanyRepository companyRepository, UserCompanyMapRepository userCompanyMapRepository,
            OtpService otpService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapRepository = userRoleMapRepository;
        this.companyRepository = companyRepository;
        this.userCompanyMapRepository = userCompanyMapRepository;
        this.otpService = otpService;
    }


    public LoginResponse login(@Valid LoginRequest request) {
        Optional<User> userOpt = userService.findUserByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid email or user not found");
        }
        User user = userOpt.get();
        
        LogUtil.setUserId(user.getUserId().toString());
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", user.getUserId());
            throw new InvalidCredentialsException("Invalid password");
        }
        
        if (!user.isVerified()) {
            return new LoginResponse(
                null, null, user.getUserId().intValue(), null, "/verify/user", null, null
            );
        }

        UserRoleMap userRole = getUserRoleMap(user);
        List<Long> companyIds = getUserCompanyIds(user);
        
        if (user.getDefaultCompany() == null) {
            log.error("User has no default company: {}", user.getUserId());
            throw new InvalidCredentialsException("User has no default company assigned");
        }
        
        LogUtil.setCompanyId(String.valueOf(user.getDefaultCompany().getCompanyId()));
        
        String token = jwtUtil.generateToken(user.getUserId(), userRole.getRole().getRoleCode(), companyIds, 
            user.getDefaultCompany().getCompanyId(), user.getDefaultCompany().getCompanyName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        
        log.info("Login successful for user: {}", user.getUserId());
        
        return new LoginResponse(
            token, refreshToken, user.getUserId().intValue(), userRole.getRole().getRoleCode(),
            userRole.getRole().getLandingUrl(), user.getDefaultCompany().getCompanyId(),
            user.getDefaultCompany().getCompanyName(), companyIds
        );
    }
    
    private UserRoleMap getUserRoleMap(User user) {
        List<UserRoleMap> userRoles = userRoleMapRepository.findActiveRolesByUserId(user.getUserId().intValue());
        if (userRoles.isEmpty()) {
            throw new InvalidCredentialsException("No active role found for user");
        }
        return userRoles.get(0);
    }
    
    private List<Long> getUserCompanyIds(User user) {
        return user.getCompanyMapping().stream()
            .filter(mapping -> mapping.getIsActive())
            .map(mapping -> mapping.getCompany().getCompanyId())
            .toList();
    }

    @Transactional
    public RegisterResponse registerNewUser(RegisterRequest dto) {
        // Check if user already exists by email
        if (userService.findUserByEmail(dto.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", dto.getEmail());
            throw new InvalidCredentialsException("User with this email already exists");
        }
        
        // Check if username already exists
        if (userRepository.findByUserNameAndIsActiveTrue(dto.getUserName()).isPresent()) {
            log.warn("Registration failed - username already exists: {}", dto.getUserName());
            throw new InvalidCredentialsException("Username already exists");
        }
        
        // 1. Create company
        Companies newCompany = new Companies();
        newCompany.setCompanyName(dto.getCompanyName());
        newCompany.setIndustry(dto.getIndustry());
        newCompany.setPan(dto.getPan());
        newCompany = companyRepository.save(newCompany);
        
        // 2. Create user
        User newUser = new User();
        newUser.setUserName(dto.getUserName());
        newUser.setFirstName(dto.getFirstName());
        newUser.setLastName(dto.getLastName());
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setDefaultCompany(newCompany);
        newUser.setVerified(false);
        newUser = userRepository.save(newUser);

        // 3. Assign default role
        UserRoleMap userRoleMap = new UserRoleMap();
        userRoleMap.setUserId(newUser.getUserId().intValue());
        userRoleMap.setRoleCode("ADM");
        userRoleMapRepository.save(userRoleMap);

        // 4. Map user to company
        UserCompanyMap userCompanyMap = new UserCompanyMap();
        userCompanyMap.setUser(newUser);
        userCompanyMap.setCompany(newCompany);
        userCompanyMapRepository.save(userCompanyMap);
        
        RegisterResponse response = new RegisterResponse();
        response.setEmail(newUser.getEmail());
        
        // Send OTP for email verification
        otpService.sendOtp(newUser.getEmail());
        log.info("Registration completed for user: {}", newUser.getUserId());
        
        return response;
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            LogUtil.setUserId(userId.toString());
            
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || !userOpt.get().getIsActive()) {
                log.warn("Token refresh failed - invalid user: {}", userId);
                throw new InvalidCredentialsException("Invalid refresh token");
            }
            
            User user = userOpt.get();
            UserRoleMap userRole = getUserRoleMap(user);
            List<Long> companyIds = getUserCompanyIds(user);
            
            String newToken = jwtUtil.generateToken(user.getUserId(), userRole.getRole().getRoleCode(), companyIds,
                user.getDefaultCompany().getCompanyId(), user.getDefaultCompany().getCompanyName());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId());
            
            return new LoginResponse(
                newToken, newRefreshToken, user.getUserId().intValue(), userRole.getRole().getRoleCode(),
                userRole.getRole().getLandingUrl(), user.getDefaultCompany().getCompanyId(),
                user.getDefaultCompany().getCompanyName(), companyIds
            );
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

}