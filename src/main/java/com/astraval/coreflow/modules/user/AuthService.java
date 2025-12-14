package com.astraval.coreflow.modules.user;

import com.astraval.coreflow.global.exception.InvalidCredentialsException;
import com.astraval.coreflow.global.exception.SystemErrorException;
import com.astraval.coreflow.modules.companies.facade.CompanyFacade;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.role.facade.RoleFacade;
import com.astraval.coreflow.modules.role.Role;
import com.astraval.coreflow.modules.user.dto.LoginRequest;
import com.astraval.coreflow.modules.user.dto.LoginResponse;
import com.astraval.coreflow.modules.user.dto.RegisterRequest;
import com.astraval.coreflow.modules.user.dto.RegisterResponse;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMapRepository;
import com.astraval.coreflow.modules.userrolemap.UserRoleMap;
import com.astraval.coreflow.modules.userrolemap.UserRoleMapRepository;

import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleMapRepository userRoleMapRepository;
    
    @Autowired
    private CompanyFacade companyFacade;
    
    @Autowired
    private RoleFacade roleFacade;
    
    
    @Autowired
    private UserCompanyMapRepository userCompanyMapRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthMapper authMapper;

    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    public LoginResponse login(LoginRequest request) {

        
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail());
        if (user == null) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Step-02: Check if the password is correct/incorrect
        // User user = userOpt.get();
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordMatches) {
            // Password mismatch for user
            throw new InvalidCredentialsException("Invalid credentials");
        }
        
        // Step-03: Get the user Role to store it in the JWT token.
        Optional<UserRoleMap> roleMapOpt = userRoleMapRepository.findByUserIdAndIsActiveTrue(user.getUserId());        
        if (roleMapOpt.isEmpty()) {
            // No role mapping found
            throw new InvalidCredentialsException("Invalid credentials");
        }
        Role role = roleMapOpt.get().getRole();
        
        // Step-04: Creating JWT token...
        try {
            // Ensure JWT secret is at least 32 bytes for HS256
            byte[] keyBytes = jwtSecret.getBytes();
            if (keyBytes.length < 32) {
                // Pad the key if it's too short
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            }
            
            // Access token (1 hour)
            String finalToken = Jwts.builder()
                    .setSubject(user.getUserId().toString())
                    .claim("roleCode", role.getRoleCode())
                    .claim("landingUrl", role.getLandingUrl())
                    .claim("companyId", user.getDefaultCompany().getCompanyId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            // Refresh token (7 days)
            String refreshToken = Jwts.builder()
                    .setSubject(user.getUserId().toString())
                    .claim("type", "refresh")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 604800000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            return authMapper.toLoginResponse(user, role, finalToken, refreshToken);
        } catch (Exception e) {
            // e.printStackTrace();
            throw new SystemErrorException("Token generation failed");
        }
    }
    
    public LoginResponse refreshToken(String refreshToken) {
        try {
            byte[] keyBytes = jwtSecret.getBytes();
            if (keyBytes.length < 32) {
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            }
            
            var claims = Jwts.parserBuilder()
                    .setSigningKey(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();
            
            String userIdStr = claims.getSubject();
            if (userIdStr == null) {
                throw new InvalidCredentialsException("Invalid token subject");
            }
            
            Integer userId;
            try {
                userId = Integer.valueOf(userIdStr);
            } catch (NumberFormatException e) {
                throw new InvalidCredentialsException("Invalid user ID format");
            }
            
            String tokenType = claims.get("type", String.class);
            
            if (!"refresh".equals(tokenType)) {
                throw new InvalidCredentialsException("Invalid token type");
            }
            
            UserRoleMap roleMap = userRoleMapRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User role not found"));
            Role role = roleMap.getRole();
            
            // Generate new access token
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
            
            String newToken = Jwts.builder()
                    .setSubject(userId.toString())
                    .claim("roleCode", role.getRoleCode())
                    .claim("landingUrl", role.getLandingUrl())
                    .claim("companyId", user.getDefaultCompany().getCompanyId())
                    .claim("companyName", user.getDefaultCompany().getCompanyName())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            return new LoginResponse(newToken, refreshToken, userId, role.getRoleCode(), role.getLandingUrl(), 
                    user.getDefaultCompany().getCompanyId(), user.getDefaultCompany().getCompanyName());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }
    
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUserNameAndIsActiveTrue(request.getUserName()) != null) {
            throw new InvalidCredentialsException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmailAndIsActiveTrue(request.getEmail()) != null) {
            throw new InvalidCredentialsException("Email already exists");
        }
        
        // Create company using facade
        Companies company = companyFacade.createCompany(request.getCompanyName(), request.getIndustry(), "SYSTEM");
        
        // Create user using mapper
        User user = authMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setContactNo("");
        user.setDefaultCompany(company);
        user.setCreatedBy("SYSTEM");
        user.setCreatedDt(LocalDateTime.now());
        user = userRepository.save(user);
        
        // Get ADMIN role via facade
        Role adminRole = roleFacade.getRoleByCode("ADM");
        if (adminRole == null) {
            throw new SystemErrorException("ADMIN role not found");
        }
        
        UserRoleMap userRoleMap = new UserRoleMap();
        userRoleMap.setUserId(user.getUserId());
        userRoleMap.setRoleId(adminRole.getRoleId());
        userRoleMap.setUser(user);
        userRoleMap.setRole(adminRole);
        userRoleMap.setCreatedBy("SYSTEM");
        userRoleMap.setCreatedDt(LocalDateTime.now());
        userRoleMapRepository.save(userRoleMap);
        
        // Create UserCompanyMap
        UserCompanyMap userCompanyMap = new UserCompanyMap();
        userCompanyMap.setUser(user);
        userCompanyMap.setCompany(company);
        userCompanyMap.setCreatedBy("SYSTEM");
        userCompanyMap.setCreatedDt(LocalDateTime.now());
        userCompanyMapRepository.save(userCompanyMap);
        
        // Generate tokens and return login response
        try {
            byte[] keyBytes = jwtSecret.getBytes();
            if (keyBytes.length < 32) {
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            }
            
            String accessToken = Jwts.builder()
                    .setSubject(user.getUserId().toString())
                    .claim("roleCode", adminRole.getRoleCode())
                    .claim("landingUrl", adminRole.getLandingUrl())
                    .claim("companyId", company.getCompanyId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            String refreshToken = Jwts.builder()
                    .setSubject(user.getUserId().toString())
                    .claim("type", "refresh")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 604800000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            return authMapper.toRegisterResponse(user, company, adminRole, accessToken, refreshToken);
        } catch (Exception e) {
            throw new SystemErrorException("Token generation failed");
        }
    }
}