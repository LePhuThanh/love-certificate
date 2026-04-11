package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LevelTrust {

    HIGH_TRUST("HIGH_TRUST","High trust"),
    MEDIUM_TRUST("MEDIUM_TRUST","Medium trust"),
    LOW_TRUST("LOW_TRUST","Low trust");

    private final String code;
    private final String description;
}
