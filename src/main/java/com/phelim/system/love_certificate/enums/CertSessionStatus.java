package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CertSessionStatus {

    DRAFT("DRAFT","DRAFT"),
    OTP_PENDING("OTP_PENDING","OTP is pending"),
    OTP_VERIFIED("VERIFIED", "OTP verified"),
    PROCESSING("PROCESSING","PROCESSING"),
    EXPIRED("EXPIRED","OTP has expired"),
    COMPLETED("COMPLETED","OTP successfully completed"),
    OTP_FAILED("OTP_FAILED","OTP failed"),
    FAILED("FAILED", "Failed in generate certificate");

    private final String code;
    private final String description;

    public static CertSessionStatus get(String value) {
        for (CertSessionStatus code : CertSessionStatus.values()) {
            if (code.getCode().equals(value)) {
                return code;
            }
        }
        return null;
    }

    public static CertSessionStatus fromCode(String value) {
        if (value == null) return null;
        for (CertSessionStatus s : values()) {
            if (s.code.equals(value)) return s;
        }
        return null;
    }
}
