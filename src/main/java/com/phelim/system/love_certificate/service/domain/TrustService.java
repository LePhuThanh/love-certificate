package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.response.CertificateTrustScoreResponse;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.CertificateVerifyLog;
import com.phelim.system.love_certificate.enums.LevelTrust;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.repository.CertificateRepository;
import com.phelim.system.love_certificate.repository.CertificateVerifyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrustService {

    private final CertificateRepository certRepo;
    private final CertificateVerifyLogRepository cerVerifyLogRepository;

    /** Cmt by Phelim (10.04.2026)
     * Cache because: scan logs + fraud detection + compute score => CPU heavy
     * Do not use sync=true because it is not as resource-intensive as publicCert() method + avoids blocking threads
     */
    @Cacheable(
            value = BaseConstants.TRUST_SCORE,
            key = "'trust:' + #certId"
    )
    public CertificateTrustScoreResponse getTrustScore(String certId) {
        log.info("[TrustService][getTrustScore] certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "certId=" + certId));

        List<CertificateVerifyLog> logs = cerVerifyLogRepository.findByCertIdOrderByVerifiedAtDesc(certId);
        // Check Revoke
        if (Boolean.TRUE.equals(cert.getRevoked())) {
            return CertificateTrustScoreResponse.builder()
                    .certId(certId)
                    .score(0) // score = 0
                    .level(LevelTrust.LOW_TRUST)
                    .revoked(true)
                    .totalVerify(logs.size())
                    .uniqueUsers(0)
                    .lastVerifiedAt(null)
                    .fraud(null)
                    .build();
        }

        // 1. Fraud detection
        boolean spam = isSpam(logs);
        boolean bot = isBot(logs);
        boolean suspiciousBurst = isSuspiciousBurst(logs);

        // 2. Engagement
        int uniqueUsers = countUniqueUsers(logs);
        int engagementScore = calculateEngagementScore(logs, uniqueUsers);

        // 3. Valid ratio (new)
        long validCount = logs.stream()
                .filter(l -> BaseConstants.VALID.equals(l.getResult()))
                .count();

        long invalidCount = logs.size() - validCount;
        double validRatio = logs.isEmpty() ? 0 : (double) validCount / logs.size();

        //Trust = integrity + behavior
        // 4. Trust score (rewrite)
        int score = (int) (
                validRatio * 50 +          // integrity
                        uniqueUsers * 10 +         // real users
                        engagementScore * 5 -      // engagement
                        (spam ? 20 : 0) -
                        (bot ? 20 : 0) -
                        (suspiciousBurst ? 10 : 0)
        );

        // clamp 0–100
        score = Math.max(0, Math.min(100, score));

        LevelTrust level = resolveLevel(score);

        LocalDateTime lastVerifiedAt = logs.isEmpty() ? null : logs.getFirst().getVerifiedAt();

        return CertificateTrustScoreResponse.builder()
                .certId(certId)
                .score(score)
                .level(level)
                .revoked(false)
                .totalVerify(logs.size())
                .uniqueUsers(uniqueUsers)
                .lastVerifiedAt(lastVerifiedAt)
                .fraud(CertificateTrustScoreResponse.FraudInfo.builder()
                        .spam(spam)
                        .bot(bot)
                        .suspicious(suspiciousBurst)
                        .build())
                .build();
    }

    // =========================
    // COMMON METHODS
    // =========================
    private boolean isSpam(List<CertificateVerifyLog> logs) {
        //1 user spam verification in a short period of time
        if (logs.isEmpty()) return false;
        Map<String, List<CertificateVerifyLog>> byFingerprint = logs.stream()
                .filter(l -> l.getFingerprint() != null)
                .collect(Collectors.groupingBy(CertificateVerifyLog::getFingerprint));

        for (Map.Entry<String, List<CertificateVerifyLog>> entry : byFingerprint.entrySet()) {
            List<CertificateVerifyLog> userLogs = entry.getValue();

            userLogs.sort(Comparator.comparing(CertificateVerifyLog::getVerifiedAt));

            for (int i = 0; i < userLogs.size(); i++) {

                LocalDateTime start = userLogs.get(i).getVerifiedAt();
                long countInWindow = userLogs.stream()
                        .filter(l -> Duration.between(start, l.getVerifiedAt()).toSeconds() <= 60)
                        .count();
                //10 requests in 1 minute
                if (countInWindow >= 10) {
                    log.warn("[TrustService][isSpam] fingerprint={} burstCount={}", entry.getKey(), countInWindow);
                    return true;
                }
            }
        }

        return false;
    }

    //detect: script + auto verify tool
    private boolean isBot(List<CertificateVerifyLog> logs) {
        //only 1 fingerprint + multiple requests + even time intervals (low variance)
        if (logs.size() < 10) return false;
        List<CertificateVerifyLog> sorted = logs.stream()
                .sorted(Comparator.comparing(CertificateVerifyLog::getVerifiedAt))
                .toList();

        long uniqueUsers = sorted.stream()
                .map(CertificateVerifyLog::getFingerprint)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        if (uniqueUsers != 1) return false;

        // check interval consistency
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < sorted.size(); i++) {
            long diff = Duration.between(
                    sorted.get(i - 1).getVerifiedAt(),
                    sorted.get(i).getVerifiedAt()
            ).toMillis();
            intervals.add(diff);
        }

        double avg = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = intervals.stream()
                .mapToDouble(i -> Math.pow(i - avg, 2))
                .average()
                .orElse(0);

        boolean lowVariance = variance < 1000; // threshold tuning
        if (lowVariance && logs.size() > 20) {
            log.warn("[TrustService][isBot] fingerprint={} variance={}", sorted.getFirst().getFingerprint(), variance);
            return true;
        }
        return false;
    }

    private boolean isSuspiciousBurst(List<CertificateVerifyLog> logs) {

        if (logs.size() < 5) return false;

        // chỉ check trong 30 giây gần nhất
        LocalDateTime now = LocalDateTime.now();

        List<CertificateVerifyLog> recent = logs.stream()
                .filter(l -> Duration.between(l.getVerifiedAt(), now).toSeconds() <= 30)
                .toList();

        if (recent.size() < 5) return false;

        Set<String> users = recent.stream()
                .map(CertificateVerifyLog::getFingerprint)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        boolean burst = users.size() >= 5;

        if (burst) {
            log.warn("[TrustService][isSuspiciousBurst] users={}, recentLogs={}",
                    users.size(), recent.size());
        }

        return burst;
    }

    private int calculateEngagementScore(List<CertificateVerifyLog> logs, int uniqueUsers) {

        int score = 0;
        // nhiều người quan tâm
        if (uniqueUsers >= 5) score += 10;
        if (uniqueUsers >= 10) score += 10;

        // nhiều lượt verify
        if (logs.size() >= 10) score += 5;
        if (logs.size() >= 30) score += 10;

        // verify hợp lệ nhiều
        long validCount = logs.stream()
                .filter(l -> BaseConstants.VALID.equals(l.getResult()))
                .count();

        if (validCount >= 5) score += 5;

        log.info("[TrustService][calculateEngagementScore] users={}, logs={}, score={}",
                uniqueUsers, logs.size(), score);

        return score;
    }

    private LevelTrust resolveLevel(int score) {
        if (score >= 80) return LevelTrust.HIGH_TRUST;
        if (score >= 50) return LevelTrust.MEDIUM_TRUST;
        return LevelTrust.LOW_TRUST;
    }

    private int countUniqueUsers(List<CertificateVerifyLog> logs) {
        return (int) logs.stream()
                .map(CertificateVerifyLog::getFingerprint)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }
}
