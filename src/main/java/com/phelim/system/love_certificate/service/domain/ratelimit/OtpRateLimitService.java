package com.phelim.system.love_certificate.service.domain.ratelimit;

import com.phelim.system.love_certificate.config.manualratelimit.OtpRateLimitConfig;
import com.phelim.system.love_certificate.config.manualratelimit.OtpRateLimitStore;
import com.phelim.system.love_certificate.config.redis.RedisRateLimitStore;
import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.ratelimit.RateLimitResult;
import com.phelim.system.love_certificate.dto.ratelimit.RateLimitRule;
import com.phelim.system.love_certificate.enums.RateLimitType;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.dto.ratelimit.RateLimitInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpRateLimitService {

    private final OtpRateLimitConfig config;
    private final RedisRateLimitStore redisRateLimitStore;
    private final OtpRateLimitStore otpRateLimitStore;

    @Value("${sms.environment}")
    private String environment;

    // Public entry
    public void checkSendOtpAllowed(String phone, String ip, String sessionId) {
        log.info("[OtpRateLimitService][checkSendOtpAllowed] phone={}, ip={}, sessionId={}",
                phone, ip, sessionId);

        if (BaseConstants.TEST_ENVIRONMENT.equalsIgnoreCase(environment)){

            // TEST OTP manual rateLimit (not redis)
            manualCheck(RateLimitType.PHONE, phone);
            manualCheck(RateLimitType.IP, ip);
            manualCheck(RateLimitType.SESSION, sessionId);

            log.info("[OtpRateLimitService][checkSendOtpAllowed] TEST environment - Skip redis server");
            return;
        }

        //LIVE => redis
        redisCheck(RateLimitType.PHONE, phone);
        redisCheck(RateLimitType.IP, ip);
        redisCheck(RateLimitType.SESSION, sessionId);
    }

    // Core check
    private void redisCheck(RateLimitType type, String value) {

        String key = "otp:rl:" + type.name() + ":" + value;

        RateLimitRule rule = config.getRule(type);
        RateLimitResult result = redisRateLimitStore.check(key, rule.getMaxAttempts(), rule.getCooldownSeconds());

        if (result.getStatus() == 0) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "Cooldown", "remainingSeconds=" + result.getRemaining());
        }

        if (result.getStatus() == -1) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "Max attempts exceeded", "type=" + type);
        }

        log.debug("[OtpRateLimitService][redisCheck] OK key={}", key);
    }

    private void manualCheck(RateLimitType type, String value) {

        String key = type.key(value);
        RateLimitRule rule = config.getRule(type);

        RateLimitInfo info = otpRateLimitStore.get(key);
        LocalDateTime now = LocalDateTime.now();

        // First time
        if (info == null) {
            otpRateLimitStore.put(key, RateLimitInfo.builder()
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
        otpRateLimitStore.put(key, info);

        log.debug("[OtpRateLimitService][check] OK type={}, key={}, attempts={}",
                type, key, info.getAttemptCount());
    }
}