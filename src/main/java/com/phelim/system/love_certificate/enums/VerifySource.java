package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerifySource {

    PUBLIC_QR("VS001", "Verify from public QR scan (user scans QR from certificate)"),

    VERIFY_HASH_API("VS002", "Verify certificate by SHA-256 hash via API"),

    VERIFY_RSA_API("VS003", "Verify certificate by RSA signature via API"),

    REVOKED_CHECK("VS004", "Verification attempt on revoked certificate");

    private final String code;
    private final String description;
}
