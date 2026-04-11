package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerifyType {
    HASH("00","Hash Verification"),
    RSA("01","Rsa Verification"),;

    private final String code;
    private final String description;
}
