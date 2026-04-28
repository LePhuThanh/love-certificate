package com.phelim.system.love_certificate.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitRule {

    private int maxAttempts;
    private int cooldownSeconds;
}
