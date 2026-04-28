package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationType {

    HASH("Verify using hash comparison"),
    RSA("Verify using RSA digital signature"),
    QR("Verify via QR code (data source)");

    private final String description;
}
