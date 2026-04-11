package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@Slf4j
public class HashSignatureService {

    public String hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(BaseConstants.ALGORITHM_SHA_256);

            byte[] hashBytes = digest.digest(data);

            return bytesToHex(hashBytes);

        } catch (Exception ex) {
            log.error("[HashSignatureService][hash] Hashing failed", ex);
            throw new BusinessException(ErrorCode.HASHING_FAILED, null);
        }
    }

    public String hashString(String input) {
        if (input == null || input.isBlank()) return null;

        return hash(input.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] bytes) {

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }
}