package com.phelim.system.love_certificate.constant;

public final class CertSessionStatus {

    private CertSessionStatus() {
        // prevent instantiation
    }

    public static final String DRAFT = "DRAFT";
    public static final String OTP_PENDING = "OTP_PENDING";
    public static final String OTP_VERIFIED = "OTP_VERIFIED";
    public static final String PROCESSING = "PROCESSING";
    public static final String EXPIRED = "EXPIRED";
    public static final String COMPLETED = "COMPLETED";
    public static final String OTP_FAILED = "OTP_FAILED";
    public static final String FAILED = "FAILED";
}
