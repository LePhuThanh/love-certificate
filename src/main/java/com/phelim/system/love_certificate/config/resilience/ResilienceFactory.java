package com.phelim.system.love_certificate.config.resilience;

import com.phelim.system.love_certificate.enums.CircuitName;
import com.phelim.system.love_certificate.enums.RetryName;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResilienceFactory {

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    // =========================
    // RETRY
    // =========================
    public Retry getRetry(RetryName name) {
        return retryRegistry.find(name.getName())
                .orElseGet(() -> retryRegistry.retry(RetryName.DEFAULT.getName()));
    }

    // =========================
    // CIRCUIT BREAKER
    // =========================
    public CircuitBreaker getCircuitBreaker(CircuitName name) {
        return circuitBreakerRegistry.find(name.getName())
                .orElseGet(() -> circuitBreakerRegistry.circuitBreaker(CircuitName.DEFAULT.getName()));
    }
}