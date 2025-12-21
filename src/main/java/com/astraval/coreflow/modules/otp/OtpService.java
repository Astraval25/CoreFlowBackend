package com.astraval.coreflow.modules.otp;

import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    public void sendOtp(String email) {
        String otp = generateOtp();
        
        otpRepository.deleteByEmail(email);
        
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtpCode(otp);
        otpVerification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepository.save(otpVerification);
        
        emailService.sendOtpEmail(email, otp);
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