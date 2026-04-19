package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RetryName {
    DEFAULT("defaultRetry"),
    SMS("smsRetry"),
    PAYMENT("paymentRetry");

    private final String name;
}