package com.phelim.system.love_certificate.dto.ratelimit;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateLimitInfo {

    private int attemptCount;
    private LocalDateTime lastSentAt;
}
