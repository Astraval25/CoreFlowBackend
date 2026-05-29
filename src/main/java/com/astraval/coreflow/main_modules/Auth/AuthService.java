package com.astraval.coreflow.main_modules.Auth;

import com.astraval.coreflow.common.exception.InvalidCredentialsException;
import com.astraval.coreflow.common.util.JwtUtil;
import com.astraval.coreflow.common.util.LogUtil;
import com.astraval.coreflow.employee_module.portaluser.EmployeePortalUser;
import com.astraval.coreflow.employee_module.portaluser.EmployeePortalUserRepository;
import com.astraval.coreflow.main_modules.Auth.dto.*;
import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.otp.OtpService;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;
import com.astraval.coreflow.main_modules.user.UserService;
import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMap;
import com.astraval.coreflow.main_modules.usercompmap.UserCompanyMapRepository;
import com.astraval.coreflow.main_modules.userrolemap.UserRoleMap;
import com.astraval.coreflow.main_modules.userrolemap.UserRoleMapRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import org.slf4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Validated
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
    private final EmployeePortalUserRepository portalUserRepository;

    public AuthService(UserRepository userRepository, UserService userService, JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder, UserRoleMapRepository userRoleMapRepository,
            CompanyRepository companyRepository, UserCompanyMapRepository userCompanyMapRepository,
            OtpService otpService, EmployeePortalUserRepository portalUserRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRoleMapRepository = userRoleMapRepository;
        this.companyRepository = companyRepository;
        this.userCompanyMapRepository = userCompanyMapRepository;
        this.otpService = otpService;
        this.portalUserRepository = portalUserRepository;
    }

    public LoginResponse login(@Valid LoginRequest request) {
        String contactNo = buildContactNumber(request.getCountryCode(), request.getPhoneNumber());
        Optional<User> userOpt = userService.findUserByContactNo(contactNo);

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Invalid phone number or user not found");
        }
        User user = userOpt.get();

        LogUtil.setUserId(user.getUserId().toString());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for user: {}", user.getUserId());
            throw new InvalidCredentialsException("Invalid password");
        }

        if (!user.isVerified()) {
            if (!hasText(user.getEmail())) {
                user.setVerified(true);
                userRepository.save(user);
            } else {
            return new LoginResponse(
                        null, null, user.getUserId().intValue(), null, "/verify/user", null, null, null,
                        user.getEmail());
            }
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
                user.getDefaultCompany().getCompanyName(), companyIds, user.getEmail());
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
        String normalizedEmail = normalizeEmail(dto.getEmail());
        String contactNo = buildContactNumber(dto.getCountryCode(), dto.getPhoneNumber());
        String requestedUserName = trimToNull(dto.getUserName());

        // Check if user already exists by phone number
        if (userService.findUserByContactNo(contactNo).isPresent()) {
            log.warn("Registration failed - phone already exists: {}", contactNo);
            throw new InvalidCredentialsException("User with this phone number already exists");
        }

        // Check if user already exists by email (when email is provided)
        if (hasText(normalizedEmail) && userService.findUserByEmail(normalizedEmail).isPresent()) {
            log.warn("Registration failed - email already exists: {}", normalizedEmail);
            throw new InvalidCredentialsException("User with this email already exists");
        }

        String finalUserName = requestedUserName;

        // Check if provided username already exists
        if (hasText(finalUserName)
                && userRepository.findByUserNameAndIsActiveTrue(finalUserName).isPresent()) {
            log.warn("Registration failed - username already exists: {}", finalUserName);
            throw new InvalidCredentialsException("Username already exists");
        }

        // Auto-generate username when omitted
        if (!hasText(finalUserName)) {
            finalUserName = generateUniqueUserName(contactNo);
        }

        // 1. Create company
        Companies newCompany = new Companies();
        newCompany.setCompanyName(dto.getCompanyName().trim());
        // companies.industry is non-null at DB level; keep a safe default when
        // registration does not provide industry.
        newCompany.setIndustry(hasText(dto.getIndustry()) ? dto.getIndustry().trim() : "General");
        newCompany.setPan(trimToNull(dto.getPan()));
        newCompany = companyRepository.save(newCompany);

        // 2. Create user
        User newUser = new User();
        newUser.setUserName(finalUserName);
        newUser.setFirstName(trimToNull(dto.getFirstName()));
        newUser.setLastName(trimToNull(dto.getLastName()));
        newUser.setEmail(normalizedEmail);
        newUser.setContactNo(contactNo);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setDefaultCompany(newCompany);
        boolean emailVerificationRequired = hasText(normalizedEmail);
        newUser.setVerified(!emailVerificationRequired);
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
        response.setEmailVerificationRequired(emailVerificationRequired);
        response.setLandingUrl(emailVerificationRequired ? "/verify/user" : "/login");

        // Send OTP for email verification only when email is provided
        if (emailVerificationRequired) {
            otpService.sendOtp(newUser.getEmail());
        }
        log.info("Registration completed for user: {}", newUser.getUserId());

        return response;
    }

    public EmployeeLoginResponse employeeLogin(EmployeeLoginRequest request) {
        EmployeePortalUser portalUser = portalUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username"));

        if (!portalUser.getIsActive()) {
            throw new InvalidCredentialsException("Portal user account is deactivated");
        }

        if (!portalUser.getEmployee().getIsActive()) {
            throw new InvalidCredentialsException("Employee is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), portalUser.getPassword())) {
            log.warn("Employee login failed - invalid password for username: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid password");
        }

        // Update last login
        portalUser.setLastLoginDt(java.time.LocalDateTime.now());
        portalUserRepository.save(portalUser);

        Long companyId = portalUser.getCompany().getCompanyId();
        String token = jwtUtil.generateEmployeeToken(
                portalUser.getPortalUserId(),
                portalUser.getEmployee().getEmployeeId(),
                companyId,
                portalUser.getCompany().getCompanyName());

        String refreshToken = jwtUtil.generateEmployeeRefreshToken(portalUser.getPortalUserId());

        log.info("Employee login successful for username: {} companyId: {}", request.getUsername(), companyId);

        return new EmployeeLoginResponse(
                token,
                refreshToken,
                portalUser.getEmployee().getEmployeeId(),
                portalUser.getEmployee().getEmployeeName(),
                portalUser.getEmployee().getEmployeeCode(),
                companyId,
                portalUser.getCompany().getCompanyName(),
                portalUser.getEmployee().getDesignation());
    }

    /**
     * Unified refresh-token handler. Reads the "type" claim to decide which flow to
     * use.
     * EMP tokens can NEVER produce a USER/admin token. USER tokens can NEVER
     * produce an EMP token.
     */
    public Object refreshToken(String refreshToken) {
        try {
            io.jsonwebtoken.Claims claims = jwtUtil.getClaims(refreshToken);
            String type = claims.get("type", String.class);

            if ("EMP".equals(type)) {
                return refreshEmployeeToken(claims);
            } else {
                return refreshUserToken(claims);
            }
        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new InvalidCredentialsException("Invalid refresh token");
        }
    }

    private LoginResponse refreshUserToken(io.jsonwebtoken.Claims claims) {
        String type = claims.get("type", String.class);

        // SECURITY: Block employee tokens from getting admin access
        if ("EMP".equals(type)) {
            log.warn("Token refresh rejected - employee token used on user refresh flow");
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        Long userId = Long.parseLong(claims.getSubject());
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
                user.getDefaultCompany().getCompanyName(), companyIds, user.getEmail());
    }

    private EmployeeLoginResponse refreshEmployeeToken(io.jsonwebtoken.Claims claims) {
        String type = claims.get("type", String.class);

        // SECURITY: Only EMP-type refresh tokens can produce employee tokens
        if (!"EMP".equals(type)) {
            log.warn("Token refresh rejected - non-employee token used on employee refresh flow");
            throw new InvalidCredentialsException("Invalid refresh token");
        }

        Long portalUserId = Long.parseLong(claims.getSubject());
        EmployeePortalUser portalUser = portalUserRepository.findById(portalUserId)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (!portalUser.getIsActive()) {
            throw new InvalidCredentialsException("Portal user account is deactivated");
        }
        if (!portalUser.getEmployee().getIsActive()) {
            throw new InvalidCredentialsException("Employee is deactivated");
        }

        Long companyId = portalUser.getCompany().getCompanyId();
        String token = jwtUtil.generateEmployeeToken(
                portalUser.getPortalUserId(),
                portalUser.getEmployee().getEmployeeId(),
                companyId,
                portalUser.getCompany().getCompanyName());

        String newRefreshToken = jwtUtil.generateEmployeeRefreshToken(portalUser.getPortalUserId());

        log.info("Employee token refreshed for portalUserId: {}", portalUserId);

        return new EmployeeLoginResponse(
                token, newRefreshToken,
                portalUser.getEmployee().getEmployeeId(),
                portalUser.getEmployee().getEmployeeName(),
                portalUser.getEmployee().getEmployeeCode(),
                companyId,
                portalUser.getCompany().getCompanyName(),
                portalUser.getEmployee().getDesignation());
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private String buildContactNumber(String countryCode, String phoneNumber) {
        if (!hasText(countryCode) || !hasText(phoneNumber)) {
            throw new InvalidCredentialsException("Country code and phone number are required");
        }

        String digitsCountryCode = countryCode.trim().replaceAll("[^0-9]", "");
        String digitsPhone = phoneNumber.trim().replaceAll("[^0-9]", "");

        if (digitsCountryCode.isEmpty()) {
            throw new InvalidCredentialsException("Invalid country code");
        }
        if (digitsPhone.length() < 6 || digitsPhone.length() > 15) {
            throw new InvalidCredentialsException("Invalid phone number");
        }

        return "+" + digitsCountryCode + digitsPhone;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String generateUniqueUserName(String contactNo) {
        String digits = contactNo.replaceAll("[^0-9]", "");
        String lastDigits = digits.length() > 8 ? digits.substring(digits.length() - 8) : digits;
        String base = "user_" + lastDigits;
        String candidate = base;
        int counter = 1;

        while (userRepository.findByUserNameAndIsActiveTrue(candidate).isPresent()) {
            candidate = base + "_" + counter++;
        }

        return candidate;
    }

}
