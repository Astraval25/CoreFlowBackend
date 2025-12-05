package com.astraval.coreflow.service;

import com.astraval.coreflow.dto.request.LoginRequest;
import com.astraval.coreflow.dto.request.RegisterRequest;
import com.astraval.coreflow.dto.response.LoginResponse;
import com.astraval.coreflow.dto.response.RegisterResponse;
import com.astraval.coreflow.exception.InvalidCredentialsException;
import com.astraval.coreflow.exception.SystemErrorException;
import com.astraval.coreflow.mapper.AuthMapper;
import com.astraval.coreflow.model.Companies;
import com.astraval.coreflow.model.Role;
import com.astraval.coreflow.model.User;
import com.astraval.coreflow.model.UserCompanyMap;
import com.astraval.coreflow.model.UserRoleMap;
import com.astraval.coreflow.repo.CompaniesRepository;
import com.astraval.coreflow.repo.RoleRepository;
import com.astraval.coreflow.repo.UserCompanyMapRepository;
import com.astraval.coreflow.repo.UserRepository;
import com.astraval.coreflow.repo.UserRoleMapRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleMapRepository userRoleMapRepository;
    
    @Autowired
    private CompaniesRepository companiesRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
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
                    .setSubject(user.getUserId())
                    .claim("roleCode", role.getRoleCode())
                    .claim("landingUrl", role.getLandingUrl())
                    .claim("companyId", user.getDefaultCompany().getCompanyId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            // Refresh token (7 days)
            String refreshToken = Jwts.builder()
                    .setSubject(user.getUserId())
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
            
            String userId = claims.getSubject();
            String tokenType = claims.get("type", String.class);
            
            if (!"refresh".equals(tokenType)) {
                throw new InvalidCredentialsException("Invalid token type");
            }
            
            UserRoleMap roleMap = userRoleMapRepository.findByUserIdAndIsActiveTrue(userId).orElseThrow();
            Role role = roleMap.getRole();
            
            // Generate new access token
            User user = userRepository.findById(userId).orElseThrow();
            
            String newToken = Jwts.builder()
                    .setSubject(userId)
                    .claim("roleCode", role.getRoleCode())
                    .claim("landingUrl", role.getLandingUrl())
                    .claim("companyId", user.getDefaultCompany().getCompanyId())
                    .claim("companyName", user.getDefaultCompany().getCompanyname())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            return new LoginResponse(newToken, refreshToken, userId, role.getRoleCode(), role.getLandingUrl(), 
                    user.getDefaultCompany().getCompanyId(), user.getDefaultCompany().getCompanyname());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }
    
    public RegisterResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUserNameAndIsActiveTrue(request.getUserName()) != null) {
            throw new InvalidCredentialsException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmailAndIsActiveTrue(request.getEmail()) != null) {
            throw new InvalidCredentialsException("Email already exists");
        }
        
        // Create company using mapper
        Companies company = authMapper.toCompany(request);
        company.setCompanyId(UUID.randomUUID().toString());
        company.setPan("");
        company.setGstNo("");
        company.setHsnCode("");
        company.setShortName("");
        company.setCreatedBy("SYSTEM");
        company.setCreatedDt(LocalDateTime.now());
        company = companiesRepository.save(company);
        
        // Create user using mapper
        User user = authMapper.toUser(request);
        user.setUserId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setContactNo("");
        user.setDefaultCompany(company);
        user.setCreatedBy("SYSTEM");
        user.setCreatedDt(LocalDateTime.now());
        user = userRepository.save(user);
        
        // Assign ADMIN role
        Role adminRole = roleRepository.findByRoleCodeAndIsActiveTrue("ADM");
        if (adminRole == null) {
            throw new SystemErrorException("ADMIN role not found");
        }
        
        UserRoleMap userRoleMap = new UserRoleMap();
        userRoleMap.setUserRoleMapId(UUID.randomUUID().toString());
        userRoleMap.setUserId(user.getUserId());
        userRoleMap.setRoleId(adminRole.getRoleId());
        userRoleMap.setUser(user);
        userRoleMap.setRole(adminRole);
        userRoleMap.setCreatedBy("SYSTEM");
        userRoleMap.setCreatedDt(LocalDateTime.now());
        userRoleMapRepository.save(userRoleMap);
        
        // Create UserCompanyMap
        UserCompanyMap userCompanyMap = new UserCompanyMap();
        userCompanyMap.setUserCompId(UUID.randomUUID().toString());
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
                    .setSubject(user.getUserId())
                    .claim("roleCode", adminRole.getRoleCode())
                    .claim("landingUrl", adminRole.getLandingUrl())
                    .claim("companyId", company.getCompanyId())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .compact();
            
            String refreshToken = Jwts.builder()
                    .setSubject(user.getUserId())
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