package com.phelim.system.love_certificate.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CertificateType {
    A("01","One year"),
    B("02","Two years"),
    C("05","Five years"),
    DEFAULT("default","Unknown");

    private final String code;
    private final String description;
}
