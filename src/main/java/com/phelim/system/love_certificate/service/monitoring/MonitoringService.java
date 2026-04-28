package com.phelim.system.love_certificate.service.monitoring;

import com.phelim.system.love_certificate.dto.monitoring.CircuitBreakerInfo;
import com.phelim.system.love_certificate.dto.monitoring.RetryInfo;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.retry.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    // Get all circuits
    public List<CircuitBreakerInfo> getCircuitBreakers() {
        log.info("[MonitoringService][getCircuitBreakers] Start");

        try {
            List<CircuitBreakerInfo> result = circuitBreakerRegistry.getAllCircuitBreakers()
                    .stream()
                    .map(cb -> {
                        CircuitBreaker.Metrics m = cb.getMetrics();

                        return CircuitBreakerInfo.builder()
                                .name(cb.getName())
                                .state(cb.getState().name())
                                .failureRate(m.getFailureRate())
                                .bufferedCalls(m.getNumberOfBufferedCalls())
                                .failedCalls(m.getNumberOfFailedCalls())
                                .successfulCalls(m.getNumberOfSuccessfulCalls())
                                .slowCalls(m.getNumberOfSlowCalls())
                                .build();
                    })
                    .collect(Collectors.toList());
            log.info("[MonitoringService][getCircuitBreakers] Success. totalCircuits={}", result.size());

            return result;
        } catch (Exception ex) {
            log.error("[MonitoringService][getCircuitBreakers] Error", ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to fetch circuit breakers");
        }
    }

    // Get all retries
    public List<RetryInfo> getRetries() {
        log.info("[MonitoringService][getRetries] Start");

        try {
            List<RetryInfo> result = retryRegistry.getAllRetries()
                    .stream()
                    .map(r -> {
                        Retry.Metrics m = r.getMetrics();

                        return RetryInfo.builder()
                                .name(r.getName())
                                .maxAttempts(r.getRetryConfig().getMaxAttempts())
                                .retryAttempts(m.getNumberOfFailedCallsWithRetryAttempt())
                                .successWithRetry(m.getNumberOfSuccessfulCallsWithRetryAttempt())
                                .successWithoutRetry(m.getNumberOfSuccessfulCallsWithoutRetryAttempt())
                                .build();
                    })
                    .collect(Collectors.toList());
            log.info("[MonitoringService][getRetries] Success. totalRetries={}", result.size());

            return result;
        } catch (Exception ex) {
            log.error("[MonitoringService][getRetries] Error", ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to fetch retries");
        }
    }

    // Get one circuit
    public CircuitBreakerInfo getCircuitBreaker(String name) {
        log.info("[MonitoringService][getCircuitBreaker] Start. name={}", name);

        try {
            CircuitBreaker cb = circuitBreakerRegistry.find(name)
                    .orElseThrow(() -> {
                        log.warn("[MonitoringService][getCircuitBreaker] Circuit not found. name={}", name);
                        return new BusinessException(ErrorCode.CIRCUIT_BREAKER_NOT_FOUND, "Circuit breaker not found", "name=" + name);
                    });

            CircuitBreaker.Metrics m = cb.getMetrics();

            CircuitBreakerInfo result = CircuitBreakerInfo.builder()
                    .name(cb.getName())
                    .state(cb.getState().name())
                    .failureRate(m.getFailureRate())
                    .bufferedCalls(m.getNumberOfBufferedCalls())
                    .failedCalls(m.getNumberOfFailedCalls())
                    .successfulCalls(m.getNumberOfSuccessfulCalls())
                    .slowCalls(m.getNumberOfSlowCalls())
                    .build();

            log.info("[MonitoringService][getCircuitBreaker] Success. name={}, state={}",
                    name, result.getState());

            return result;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[MonitoringService][getCircuitBreaker] Error. name={}", name, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to fetch circuit", "name=" + name);
        }
    }

    // Reset circuit
    public void resetCircuit(String name) {
        log.warn("[MonitoringService][resetCircuit] Start. name={}", name);

        try {
            CircuitBreaker cb = circuitBreakerRegistry.find(name)
                    .orElseThrow(() -> {
                        log.warn("[MonitoringService][resetCircuit] Circuit not found. name={}", name);
                        return new BusinessException(ErrorCode.DATA_NOT_FOUND, "Circuit breaker not found", "name=" + name);
                    });
            cb.reset();
            log.warn("[MonitoringService][resetCircuit] Reset SUCCESS. name={}", name);

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[MonitoringService][resetCircuit] Error. name={}", name, ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to reset circuit", "name=" + name);
        }
    }
}