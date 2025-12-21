package com.astraval.coreflow.modules.Auth;

import com.astraval.coreflow.common.exception.InvalidCredentialsException;
import com.astraval.coreflow.common.util.JwtUtil;
import com.astraval.coreflow.modules.Auth.dto.LoginRequest;
import com.astraval.coreflow.modules.Auth.dto.LoginResponse;
import com.astraval.coreflow.modules.Auth.dto.RegisterRequest;
import com.astraval.coreflow.modules.Auth.dto.RegisterResponse;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserService;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.modules.userrolemap.UserRoleMap;
import com.astraval.coreflow.modules.userrolemap.UserRoleMapRepository;

import jakarta.transaction.Transactional;
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
    
    @Autowired
    private CompanyRepository companyRepository;

    public LoginResponse login(@Valid LoginRequest request) {
        Optional<User> userOpt = userService.findUserByEmail(request.getEmail());
        
        // Check the user name is correct 
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("In valid gmail... or User Not Found...");
        }
        User user = userOpt.get();
        
        // check the password is correct
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("In valid Password..");
        }
        
        if (user.isVerified() == false) {
            return new LoginResponse(
                    null,
                    null,
                    user.getUserId().intValue(),
                    null,
                    "/verify/user",

                    null,
                    null);
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
            .map(mapping -> mapping.getCompany().getCompanyId())
            .toList();
    }
    
    private String getRoleLandingUrl(User user) {
        List<UserRoleMap> userRoles = userRoleMapRepository.findActiveRolesByUserId(user.getUserId().intValue());
        if (userRoles.isEmpty()) {
            throw new InvalidCredentialsException("No active role found for user");
        }
        return userRoles.get(0).getRole().getLandingUrl();
    }

    @Transactional
    public RegisterResponse registerNewUser(RegisterRequest dto) {
        // 1. Create company
        Companies newCompany = new Companies();
        newCompany.setCompanyName(dto.getCompanyName());
        newCompany.setIndustry(dto.getIndustry());
        newCompany.setPan(dto.getPan());
        companyRepository.save(newCompany);
        // 2. Create user

        // 3. Set default company

        // 4. Assign default role

        // 5. Map user to company

        // 6. Return response
        return new RegisterResponse();
    }

}