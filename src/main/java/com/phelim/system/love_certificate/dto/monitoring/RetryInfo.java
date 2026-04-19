package com.phelim.system.love_certificate.dto.monitoring;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RetryInfo {

    private String name;

    private int maxAttempts;

    private long retryAttempts;
    private long successWithRetry;
    private long successWithoutRetry;
}
