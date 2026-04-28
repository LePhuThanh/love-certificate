package com.phelim.system.love_certificate.dto.ratelimit;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateLimitResult {
    private Long status;   // 1=OK, 0=COOLDOWN, -1=MAX
    private Long remaining;
}
 