package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.config.LoveCertificateProperties;
import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ClientService clientService;
    private final LoveCertificateProperties loveCertProperties;

    @Value("${sms.environment}")
    private String environment;

    public void sendOtp(String phoneNumber, String otp) {
        log.info("[OtpService][sendOtp] Sending OTP to phone: {}, OTP: {}", phoneNumber, otp);

        // Build message
        String message = "Quy khach vui long nhap " + otp + " de xac nhan lien ket. Hotline 19006679.";
        log.info("[OtpService][sendOtp] Generated OTP={}, phoneNumber={}", otp, phoneNumber);

        // Send SMS
        if (BaseConstants.TEST_ENVIRONMENT.equalsIgnoreCase(environment)){
            log.info("[OtpService][sendOtp] TEST environment - Skip sending SMS");
        } else {
            clientService.sendOtp(phoneNumber, message);
        }

        log.info("[OtpService][sendOtp] Otp sent successfully to: {}", phoneNumber);
    }

    // =========================
    // HASH OTP
    // =========================

    // 1. Generate Otp
    public String generateOtp() {
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        log.debug("[OtpService][generateOtp] Generated OTP");
        return String.valueOf(otp);
    }

    // 2. Generate salt
    public byte[] generateSalt() {
        byte[] salt = new byte[loveCertProperties.getOtpSaltLength()];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    // 3. Hash Otp (PBKDF2)
    public String hashOtp(String otp, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    otp.toCharArray(),
                    salt,
                    loveCertProperties.getIterations(),
                    loveCertProperties.getKeyLength());

            SecretKeyFactory factory = SecretKeyFactory.getInstance(BaseConstants.PBKDF2_WITH_HMAC_SHA256);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            log.error("[OtpService][hashOtp] Error hashing OTP", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, null);
        }
    }

    // 4. Encode / Decode salt
    public String encodeSalt(byte[] salt) {
        return Base64.getEncoder().encodeToString(salt);
    }

    public byte[] decodeSalt(String saltStr) {
        return Base64.getDecoder().decode(saltStr);
    }

    // 5. Verify otp
    public boolean verifyOtp(String inputOtp, String storedHash, String storedSalt) {
        try {
            byte[] salt = decodeSalt(storedSalt);
            String hashedInput = hashOtp(inputOtp, salt);

            return constantTimeEquals(
                    Base64.getDecoder().decode(storedHash),
                    Base64.getDecoder().decode(hashedInput)
            );

        } catch (Exception e) {
            log.error("[OtpService][verifyOtp] Error verifying OTP", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, null);
        }
    }

    // 6. Constant time compare
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }
}
