package com.phelim.system.love_certificate.dto.monitoring;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CircuitBreakerInfo {

    private String name;
    private String state;

    private float failureRate;

    private int bufferedCalls;
    private int failedCalls;
    private int successfulCalls;
    private int slowCalls;
}