package com.ecommerce.project.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class OtpGeneratorService {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}
