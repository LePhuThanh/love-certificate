package com.phelim.system.love_certificate.config.resilience;

import com.phelim.system.love_certificate.enums.CircuitName;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.SimulatedTimeoutException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class CircuitBreakerConfigSetup {

    // =========================
    // CIRCUIT BREAKER
    // =========================
    /**
     * Request
     *    ↓
     * Retry attempts (3 times)
     *    ↓
     * FINAL RESULT (SUCCESS / FAILURE)
     *    ↓
     * CircuitBreaker record (1 time)
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {

        // Default config
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(15))

                // Count all exceptions as failures
                .recordExceptions(Exception.class)

                // Ignore business logic exceptions (do not count as failures)
                .ignoreExceptions(BusinessException.class)

                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);

        // ------------------------CREATE INSTANCES------------------------
        // Default instance
        CircuitBreaker defaultCb = registry.circuitBreaker(CircuitName.DEFAULT.getName(), defaultConfig);

        // SMS circuit
        CircuitBreaker smsCb = registry.circuitBreaker(CircuitName.SMS.getName(), CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(80)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .recordExceptions(
                        feign.RetryableException.class,
                        IOException.class,
                        SimulatedTimeoutException.class
                )
                .ignoreExceptions(BusinessException.class)
                .build()
        );

        // Payment circuit
        CircuitBreaker paymentCb = registry.circuitBreaker(CircuitName.PAYMENT.getName(), CircuitBreakerConfig.custom()
                .slidingWindowSize(5)
                .failureRateThreshold(50)
                .minimumNumberOfCalls(3)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .recordExceptions(TimeoutException.class)
                .ignoreExceptions(BusinessException.class)
                .build()
        );

        // ------------------------ATTACH LOGGING------------------------
        attachCircuitLogging(defaultCb);
        attachCircuitLogging(smsCb);
        attachCircuitLogging(paymentCb);
        log.info("[CircuitBreakerConfigSetup][circuitBreakerRegistry] CircuitBreaker registry initialized");

        return registry;
    }

    // Attach logging for all circuit-breaker
    private void attachCircuitLogging(CircuitBreaker cb) {

        cb.getEventPublisher()
                // STATE CHANGE
                .onStateTransition(e -> {
                    CircuitBreaker.Metrics metrics = cb.getMetrics();
                    log.warn(
                            "[Resilience][CIRCUIT-BREAKER]----------[STATE] " +
                                    "CircuitName={}, state changed:{} => {} | total={}, failed={}, success={}, slow={}, failureRate={}%",
                            e.getCircuitBreakerName(),
                            e.getStateTransition().getFromState(),
                            e.getStateTransition().getToState(),
                            metrics.getNumberOfBufferedCalls(),
                            metrics.getNumberOfFailedCalls(),
                            metrics.getNumberOfSuccessfulCalls(),
                            metrics.getNumberOfSlowCalls(),
                            metrics.getFailureRate()
                    );
                })
                // FAILURE RATE EXCEEDED
                .onFailureRateExceeded(e -> {
                    CircuitBreaker.Metrics metrics = cb.getMetrics();
                    log.error(
                            "[Resilience][CIRCUIT-BREAKER]----------[THRESHOLD] " +
                                    "CircuitName={}, FAILURE_RATE_EXCEEDED={}%% | total={}, failed={}, success={}",
                            e.getCircuitBreakerName(),
                            e.getFailureRate(),
                            metrics.getNumberOfBufferedCalls(),
                            metrics.getNumberOfFailedCalls(),
                            metrics.getNumberOfSuccessfulCalls()
                    );
                })
                // CALL BLOCKED (OPEN)
                .onCallNotPermitted(e -> {
                    CircuitBreaker.Metrics metrics = cb.getMetrics();
                    log.error(
                            "[Resilience][CIRCUIT-BREAKER]----------[BLOCKED] " +
                                    "CircuitName={}, CALL_BLOCKED (OPEN) | total={}, failed={}, success={}, failureRate={}%",
                            e.getCircuitBreakerName(),
                            metrics.getNumberOfBufferedCalls(),
                            metrics.getNumberOfFailedCalls(),
                            metrics.getNumberOfSuccessfulCalls(),
                            metrics.getFailureRate()
                    );
                })
                // ERROR (FAILURE RECORDED)
                .onError(e -> {
                    CircuitBreaker.Metrics metrics = cb.getMetrics();
                    log.warn(
                            "[Resilience][CIRCUIT-BREAKER]----------[ERROR] " +
                                    "CircuitName={} ERROR recorded ex={} | total={}, failed={}, failureRate={}%",
                            e.getCircuitBreakerName(),
                            getEx(e.getThrowable()),
                            metrics.getNumberOfBufferedCalls(),
                            metrics.getNumberOfFailedCalls(),
                            metrics.getFailureRate()
                    );
                })
                // SUCCESS
                .onSuccess(e -> {
                    CircuitBreaker.Metrics metrics = cb.getMetrics();
                    log.debug(
                            "[Resilience][CIRCUIT-BREAKER]----------[SUCCESS] " +
                                    " CircuitName={} SUCCESS call | total={}, success={}, failureRate={}%",
                            e.getCircuitBreakerName(),
                            metrics.getNumberOfBufferedCalls(),
                            metrics.getNumberOfSuccessfulCalls(),
                            metrics.getFailureRate()
                    );
                });
    }

    private String getEx(Throwable t) {
        return t == null ? "N/A" : t.getClass().getSimpleName();
    }
}
