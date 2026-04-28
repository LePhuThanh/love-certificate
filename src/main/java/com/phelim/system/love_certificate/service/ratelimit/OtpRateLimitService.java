package com.phelim.system.love_certificate.service.ratelimit;

import com.phelim.system.love_certificate.dto.RateLimitRule;
import com.phelim.system.love_certificate.enums.RateLimitType;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.dto.RateLimitInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpRateLimitService {

    private final OtpRateLimitStore store;
    private final OtpRateLimitConfig config;

    // Public entry
    public void checkSendOtpAllowed(String phone, String ip, String sessionId) {
        log.info("[OtpRateLimitService][checkSendOtpAllowed] phone={}, ip={}, sessionId={}",
                phone, ip, sessionId);

        check(RateLimitType.PHONE, phone);
        check(RateLimitType.IP, ip);
        check(RateLimitType.SESSION, sessionId);
    }

    // Core check
    private void check(RateLimitType type, String value) {

        String key = type.key(value);
        RateLimitRule rule = config.getRule(type);

        RateLimitInfo info = store.get(key);
        LocalDateTime now = LocalDateTime.now();

        // First time
        if (info == null) {
            store.put(key, RateLimitInfo.builder()
                    .attemptCount(1)
                    .lastSentAt(now)
                    .build());

            log.debug("[OtpRateLimitService][check] FIRST type={}, key={}", type, key);
            return;
        }

        // Cooldown check
        long seconds = ChronoUnit.SECONDS.between(info.getLastSentAt(), now);

        if (seconds < rule.getCooldownSeconds()) {
            int remaining = (int) (rule.getCooldownSeconds() - seconds);

            log.warn("[OtpRateLimitService][check] COOLDOWN type={}, key={}, remaining={}s",
                    type, key, remaining);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "Too many requests",
                    String.format("type=%s, remainingSeconds=%s", type, remaining));
        }

        // Max attempt
        if (info.getAttemptCount() >= rule.getMaxAttempts()) {
            log.error("[OtpRateLimitService][check] MAX_ATTEMPT type={}, key={}", type, key);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "Rate limit exceeded", "type=" + type);
        }

        // Update
        info.setAttemptCount(info.getAttemptCount() + 1);
        info.setLastSentAt(now);
        store.put(key, info);

        log.debug("[OtpRateLimitService][check] OK type={}, key={}, attempts={}",
                type, key, info.getAttemptCount());
    }
}