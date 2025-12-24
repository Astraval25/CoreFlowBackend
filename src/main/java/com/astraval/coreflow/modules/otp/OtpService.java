package com.astraval.coreflow.modules.otp;

import com.astraval.coreflow.common.util.EmailUtil;
import com.astraval.coreflow.modules.emailtemplate.EmailTemplateService;
import com.astraval.coreflow.modules.emailtemplate.dto.EmailTemplateDetails;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private OtpRepository otpRepository;
    private EmailUtil emailService;
    private UserRepository userRepository;
    private EmailTemplateService emailTemplateService;

    public OtpService(OtpRepository otpRepository, EmailUtil emailService, UserRepository userRepository, EmailTemplateService emailTemplateService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.emailTemplateService = emailTemplateService;
    }

    @Transactional
    public void sendOtp(String email) {
        // Check if user is already verified
        Optional<User> userOpt = userRepository.findActiveUserByEmail(email);
        if (userOpt.isPresent() && userOpt.get().isVerified()) {
            throw new RuntimeException("User already verified");
        }
        
        String otp = generateOtp();
        
        otpRepository.deleteByEmail(email);
        
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otpVerification);
        
        // Get Emil Template for OTP sending
        EmailTemplateDetails details = emailTemplateService.getEmailTemplateByName("OTP Verification");
        String bodyTextData = details.getBodyText().replace("{{otp}}", otp);
                
        emailService.sendTextEmail(email, details.getSubject(), bodyTextData);
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<OtpVerification> otpOpt = otpRepository.findByEmailAndOtpCodeAndIsVerifiedFalse(email, otp);
        
        if (otpOpt.isEmpty()) {
            return false;
        }
        
        OtpVerification otpVerification = otpOpt.get();
        
        if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        otpVerification.setIsVerified(true);
        otpRepository.save(otpVerification);
        
        Optional<User> userOpt = userRepository.findActiveUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(true);
            userRepository.save(user);
        }
        
        return true;
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}