package com.phelim.system.love_certificate.config.manualratelimit;

import com.phelim.system.love_certificate.dto.ratelimit.RateLimitRule;
import com.phelim.system.love_certificate.enums.RateLimitType;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Component
public class OtpRateLimitConfig {

    private final Map<RateLimitType, RateLimitRule> rules = Map.of(
            RateLimitType.PHONE, RateLimitRule.builder()
                    .maxAttempts(5)
                    .cooldownSeconds(20)
                    .build(),

            RateLimitType.IP, RateLimitRule.builder()
                    .maxAttempts(20)
                    .cooldownSeconds(60)
                    .build(),

            RateLimitType.SESSION, RateLimitRule.builder()
                    .maxAttempts(10)
                    .cooldownSeconds(60)
                    .build()
    );

    public RateLimitRule getRule(RateLimitType type) {
        return rules.get(type);
    }
}
