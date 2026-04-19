package com.phelim.system.love_certificate.config.resilience;

import com.phelim.system.love_certificate.enums.RetryName;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.SimulatedTimeoutException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
@Slf4j
public class RetryConfigSetup {

    // =========================
    // RETRY
    // =========================
    @Bean
    public RetryRegistry retryRegistry() {

        // Default config (fallback for all)
        RetryConfig defaultConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500)) //0.5s
                .retryExceptions(Exception.class)
                .ignoreExceptions(BusinessException.class)
                .build();

        RetryRegistry registry = RetryRegistry.of(defaultConfig);

        // ------------------------CREATE INSTANCES------------------------
        // Default instance
        Retry defaultRetry = registry.retry(RetryName.DEFAULT.getName(), defaultConfig);

        // SMS retry
        Retry smsRetry = registry.retry(RetryName.SMS.getName(), RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))

                // Only retry for technical exception
                .retryExceptions(
                        feign.RetryableException.class,
                        IOException.class,
                        SimulatedTimeoutException.class
                )

                // No retry, business exception
                .ignoreExceptions(BusinessException.class)
                .build()
        );

        // Payment retry (Example)
        Retry paymentRetry = registry.retry(RetryName.PAYMENT.getName(), RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(300))
                .retryExceptions(TimeoutException.class)
                .ignoreExceptions(BusinessException.class)
                .build()
        );

        // ------------------------ATTACH LOGGING------------------------
        attachRetryLogging(defaultRetry);
        attachRetryLogging(smsRetry);
        attachRetryLogging(paymentRetry);
        log.info("[RetryConfigSetup][retryRegistry] Retry registry initialized");

        return registry;
    }

    // Attach logging for all retry
    private void attachRetryLogging(Retry retry) {
        int maxAttempts = retry.getRetryConfig().getMaxAttempts();

        retry.getEventPublisher()
                // ATTEMPT
                .onRetry(e ->
                        log.warn(
                                "[Resilience][RETRY]----------[ATTEMPT] RetryName={}, attempt={}/{}, wait={}ms, ex={}",
                                e.getName(),
                                e.getNumberOfRetryAttempts(),
                                maxAttempts,
                                e.getWaitInterval().toMillis(),
                                getEx(e.getLastThrowable())
                        ))
                // FINAL FAILURE (all retries exhausted)
                .onError(e -> log.error(
                        "[Resilience][RETRY]----------[EXHAUSTED] " +
                                "RetryName={}, totalAttempts={}/{}, finalEx={}",
                        e.getName(),
                        e.getNumberOfRetryAttempts(),
                        maxAttempts,
                        getEx(e.getLastThrowable())
                ))
                // SUCCESS (after retry success)
                .onSuccess(e -> log.info(
                        "[Resilience][RETRY]----------[SUCCESS] " +
                                "RetryName={} SUCCESS after totalAttempts={}",
                        e.getName(),
                        e.getNumberOfRetryAttempts()
                ));
    }

    private String getEx(Throwable t) {
        return t == null ? "N/A" : t.getClass().getSimpleName();
    }
}