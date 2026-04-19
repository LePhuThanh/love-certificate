package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CircuitName {
    DEFAULT("defaultCircuit"),
    SMS("smsCircuit"),
    PAYMENT("paymentCircuit");

    private final String name;
}