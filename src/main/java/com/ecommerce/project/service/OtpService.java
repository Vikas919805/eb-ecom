package com.ecommerce.project.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.project.model.OtpCode;
import com.ecommerce.project.repositories.OtpRepository;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private OtpGeneratorService otpGeneratorService;

    @Autowired
    private EmailService emailService;

    public String generateAndSendOtp(String email) {
        String otp = otpGeneratorService.generateOtp();

        OtpCode otpCode = new OtpCode();
        otpCode.setEmail(email);
        otpCode.setCode(otp);
        otpCode.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpCode.setUsed(false);
        otpCode.setAttempts(0);

        otpRepository.save(otpCode);

        try {
            emailService.sendOtp(email, otp);
        } catch (Exception e) {
            return "OTP saved in database, but email sending failed: " + e.getMessage();
        }

        return "OTP sent successfully";
    }

    public String verifyOtp(String email, String otp) {
        OtpCode savedOtp = otpRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new RuntimeException("OTP not found for email: " + email));

        if (savedOtp.isUsed()) {
            throw new RuntimeException("OTP already used");
        }

        if (savedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!savedOtp.getCode().equals(otp)) {
            savedOtp.setAttempts(savedOtp.getAttempts() + 1);
            otpRepository.save(savedOtp);
            throw new RuntimeException("Invalid OTP");
        }

        savedOtp.setUsed(true);
        otpRepository.save(savedOtp);

        return "OTP verified successfully";
    }
}