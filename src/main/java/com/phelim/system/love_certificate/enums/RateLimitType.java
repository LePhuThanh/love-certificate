package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RateLimitType {

    PHONE("PHONE"),
    IP("IP"),
    SESSION("SESSION");

    private final String prefix;

    public String key(String value) {
        return prefix + ":" + value;
    }
}
