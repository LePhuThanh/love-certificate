package com.phelim.system.love_certificate.enums;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Region {

    // =========================
    // Southeast Asia (SEA)
    // =========================
    VN("VN", "Vietnam"),
    TH("TH", "Thailand"),
    SG("SG", "Singapore"),
    MY("MY", "Malaysia"),
    ID("ID", "Indonesia"),
    PH("PH", "Philippines"),
    LA("LA", "Laos"),
    KH("KH", "Cambodia"),
    MM("MM", "Myanmar"),
    BN("BN", "Brunei"),

    // =========================
    // East Asia
    // =========================
    CN("CN", "China"),
    JP("JP", "Japan"),
    KR("KR", "South Korea"),
    HK("HK", "Hong Kong"),
    TW("TW", "Taiwan"),

    // =========================
    // South Asia
    // =========================
    IN("IN", "India"),
    PK("PK", "Pakistan"),
    BD("BD", "Bangladesh"),
    LK("LK", "Sri Lanka"),
    NP("NP", "Nepal"),

    // =========================
    // North America
    // =========================
    US("US", "United States"),
    CA("CA", "Canada"),

    // =========================
    // Europe (common)
    // =========================
    GB("GB", "United Kingdom"),
    DE("DE", "Germany"),
    FR("FR", "France"),
    IT("IT", "Italy"),
    ES("ES", "Spain"),
    NL("NL", "Netherlands"),

    // =========================
    // Oceania
    // =========================
    AU("AU", "Australia"),
    NZ("NZ", "New Zealand"),

    // =========================
    // Middle East
    // =========================
    AE("AE", "United Arab Emirates"),
    SA("SA", "Saudi Arabia"),
    TR("TR", "Turkey");

    private final String code;
    private final String message;

    Region(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Convert from string safely (case-insensitive)
     */
    public static Region from(String value) {
        return Arrays.stream(values())
                .filter(r -> r.code.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid region code: " + value)
                );
    }
}
