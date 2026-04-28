package com.phelim.system.love_certificate.service.application;

import com.phelim.system.love_certificate.config.LoveCertificateProperties;
import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.request.*;
import com.phelim.system.love_certificate.dto.response.*;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.CertificateSession;
import com.phelim.system.love_certificate.entity.CertificateVerifyLog;
import com.phelim.system.love_certificate.entity.LoveStory;
import com.phelim.system.love_certificate.enums.CertSessionStatus;
import com.phelim.system.love_certificate.enums.CertificateType;
import com.phelim.system.love_certificate.enums.VerifyType;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.repository.CertificateRepository;
import com.phelim.system.love_certificate.repository.CertificateSessionRepository;
import com.phelim.system.love_certificate.repository.CertificateVerifyLogRepository;
import com.phelim.system.love_certificate.repository.LoveStoryRepository;
import com.phelim.system.love_certificate.service.domain.*;
import com.phelim.system.love_certificate.service.ratelimit.OtpRateLimitService;
import com.phelim.system.love_certificate.util.LoveCertificateUtil;
import com.phelim.system.love_certificate.util.PhoneNumberNormalizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final CertificateSessionRepository sessionRepo;
    private final CertificateRepository certRepo;
    private final RuleEngineService ruleEngine;
    private final PdfService pdfService;
    private final TemplateService templateService;
    private final HashSignatureService hashSignatureService;
    private final RsaSignatureService rsaSignatureService;
    private final CertificateVerifyLogRepository cerVerifyLogRepository;
    private final LoveStoryRepository loveStoryRepository ;
    private final TrustService trustService;
    private final TimelineService timelineService;
    private final VerifyLogService verifyLogService;
    private final PhoneNumberNormalizer phoneNumberNormalizer;
    private final OtpService otpService;
    private final LoveCertificateProperties loveCertificateProperties;
    private final CertificateAsyncService certAsyncService;
    private final OtpRateLimitService otpRateLimitService;

    private final CacheManager cacheManager;

    // =========================
    // METHOD
    // =========================
    @Override
    public CertificateSession init(InitRequest req) {
        log.info("[CertificateServiceImpl][init] Start. requestId={}", req.getRequestId());

        // Check idempotency
        Optional<CertificateSession> existing = sessionRepo.findByRequestId(req.getRequestId());
        if (existing.isPresent()) {

            CertificateSession session = existing.get();
            if (!isSameRequest(session, req)) {
                log.warn("[CertificateServiceImpl][init] Idempotency conflict requestId={}", req.getRequestId());
                throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REUSED, "requestId=" + req.getRequestId());
            }
            return session;
        }
        String phoneE164Format = phoneNumberNormalizer.toE164(req.getPhoneNumber(), req.getRegion()); // 0987123456 => +84987123456
        log.debug("[CertificateServiceImpl][init] phone={} to phoneE164Format={}, region={}", req.getPhoneNumber(), phoneE164Format, req.getRegion());

        CertificateSession session = CertificateSession.builder()
                .sessionId(generateSessionId())
                .requestId(req.getRequestId())
                .maleName(req.getMaleName())
                .femaleName(req.getFemaleName())
                .maleAge(req.getMaleAge())
                .femaleAge(req.getFemaleAge())
                .loveStartDate(req.getLoveStartDate())
                .status(CertSessionStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .email(req.getEmail())
                .phoneNumber(phoneE164Format)
                .region(req.getRegion())
                .build();

        sessionRepo.save(session);
        return session;
    }

    @Override
    public byte[] previewCertificate(PreviewCertRequest req) {
        log.info("[CertificateServiceImpl][previewCertificate] Start. requestId={}, sessionId={}", req.getRequestId(), req.getSessionId());

        CertificateSession session = sessionRepo.findById(req.getSessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND, "sessionId=" + req.getSessionId()));

        if (!CertSessionStatus.DRAFT.equals(session.getStatus())) {
            log.warn("[CertificateServiceImpl][previewCertificate] Preview only allowed in DRAFT. requestId={}, sessionId={}", req.getRequestId(), req.getSessionId());
            throw new BusinessException(ErrorCode.INVALID_STATE, "Preview only allowed in DRAFT", "sessionId=" + session.getSessionId());
        }

        //Calculate loving duration
        int days = (int) ChronoUnit.DAYS.between(
                session.getLoveStartDate(),
                LocalDate.now()
        );

        CertificateType type = ruleEngine.determineType(days);

        Certificate temp = Certificate.builder()
                .certId(BaseConstants.TEMP)
                .sessionId(session.getSessionId())
                .durationDays(days)
                .type(type)
                .build();

        String html = templateService.renderCertificate(temp, session, "preview");

        return pdfService.generatePdf(html);
    }

    @Override
    public void sendOtp(GenerateRequest req, HttpServletRequest httpRequest) {
        log.info("[CertificateServiceImpl][sendOtp] Start. requestId={}, sessionId={}", req.getRequestId(), req.getSessionId());

        // Rate limit check
        otpRateLimitService.checkSendOtpAllowed(
                req.getPhoneNumber(),
                getClientIp(httpRequest),
                req.getSessionId()
        );

        CertificateSession session = getSession(req.getSessionId());
        if (!CertSessionStatus.DRAFT.equals(session.getStatus())) {
            log.warn("[CertificateServiceImpl][sendOtp] Preview only allowed in DRAFT. requestId={}, sessionId={}", req.getRequestId(), req.getSessionId());
            throw new BusinessException(ErrorCode.INVALID_STATE, "Preview only allowed in DRAFT", "sessionId=" + session.getSessionId());
        }

        // Validate phoneNumber
        String phoneNumber = req.getPhoneNumber();
        String phoneE164Format = phoneNumberNormalizer.toE164(phoneNumber, req.getRegion());

        String corePhoneE164Format = session.getPhoneNumber();
        String corePhoneNumber = phoneNumberNormalizer.toNational(corePhoneE164Format, req.getRegion());

        if(!phoneE164Format.equals(corePhoneE164Format)){
            log.warn("[CertificateServiceImpl][sendOtp] The provided phone number mismatch the core phone number. phoneNumber={}, corePhoneNumber={}, region={}, requestId={}, sessionId={}",
                    phoneNumber, corePhoneNumber, req.getRegion(), req.getRequestId(), req.getSessionId());
            throw new BusinessException(ErrorCode.PHONE_MISMATCH, String.format("phoneNumber=%s, corePhoneNumber=%s, region=%s, requestId=%s, sessionId=%s",
                    phoneNumber, corePhoneNumber, req.getRegion(), req.getRequestId(), req.getSessionId()));
        }
        //Otp Generation
//        String otp = otpService.generateOtp();
//        byte[] salt = otpService.generateSalt();
//        String otpHash = otpService.hashOtp(otpCode, salt);
//        String otpSalt = otpService.encodeSalt(salt);

        String otp = "123456";

        // Send OTP via SMS (Blocking)
        otpService.sendOtp(phoneNumber, otp);

        // Save OTP only after success
        session.setOtp(otp);
//        session.setOtpHash(otpHash);
//        session.setOtpSalt(otpSalt);
        session.setOtpExpireAt(LocalDateTime.now().plusMinutes(2));
        session.setStatus(CertSessionStatus.OTP_PENDING);
        session.setUpdatedAt(LocalDateTime.now());

        sessionRepo.save(session);

        log.info("[CertificateServiceImpl][sendOtp] Otp={} sent & saved SUCCESS. sessionId={}, otpExpireAt={}",
                otp, session.getSessionId(), session.getOtpExpireAt());
    }

    /**
     *
     1. DB-level lock (most important)
     FOR UPDATE => only one thread can process the session
     2. afterCommit => prevents rollback bugs
     async only runs when the DB has committed
     3. Async still has its own lock
     double protection (defensive design)
     4. Idempotent check
     PROCESSING / COMPLETED => return
     */
    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest req) {
        log.info("[CertificateServiceImpl][verifyOtp] Start. sessionId={}, otp={}", req.getSessionId(), req.getOtp());
        log.debug("-------------------------------Thread verifyOtp: {}", Thread.currentThread().getName());
        log.debug("START [CertificateServiceImpl][verifyOtp] {}", System.currentTimeMillis());

        final int MAX_OTP_RETRY = loveCertificateProperties.getMaxOtpRetry(); //5

        // Validate sessionId exists - LOCK session here
        CertificateSession session = getSessionForUpdate(req.getSessionId());
        // Validate requestId unique

        // Idempotent check FIRST (after lock)
        // Validate certificate session not completed //COMPLETED (generated cert) => return success (idempotent)
        if (session.isCompleted()) {
            log.info("[CertificateServiceImpl][verifyOtp] Certificate session already completed. sessionId={}", req.getSessionId());

            Optional<Certificate> existingCert = certRepo.findBySessionId(req.getSessionId());
            if (existingCert.isEmpty()) {
                log.warn("[CertificateServiceImpl][verifyOtp] Certificate not found. sessionId={}", req.getSessionId());
                throw new BusinessException(ErrorCode.CERTIFICATE_NOT_FOUND, "sessionId=" + req.getSessionId());
            }
            return;
        }

        // Validate certificate session only OTP_PENDING status
        if (!CertSessionStatus.OTP_PENDING.equals(session.getStatus())) {
            log.warn("[CertificateServiceImpl][verifyOtp] Only allowed in OTP_PENDING. sessionId={}", req.getSessionId());
            throw new BusinessException(ErrorCode.INVALID_STATE, "verifyOtp only allowed in OTP_PENDING", "sessionId=" + session.getSessionId());
        }

        // Validate OTP not expired
        if(session.isOtpExpired()){
            if (!CertSessionStatus.EXPIRED.equals(session.getStatus())) {
                session.setStatus(CertSessionStatus.EXPIRED);
                session.incrementRetryCount();
                session.setUpdatedAt(LocalDateTime.now());
                sessionRepo.save(session);
            }
            log.warn("[CertificateServiceImpl][verifyOtp] OTP expired for sessionId: {}", req.getSessionId());
            throw new BusinessException(ErrorCode.OTP_EXPIRED, "sessionId=" + req.getSessionId());
        }

        // Validate max retry exceeded — Only update status OTP_FAILED, no incrementRetryCount
        if (session.isMaxRetryExceeded(MAX_OTP_RETRY)) {
            if (!session.getStatus().equals(CertSessionStatus.OTP_FAILED)) {
                session.setStatus(CertSessionStatus.OTP_FAILED);
                session.setUpdatedAt(LocalDateTime.now());
                sessionRepo.save(session);
            }
            log.warn("[CertificateServiceImpl][verifyOtp] Max retry exceeded for sessionId: {}", req.getSessionId());
            throw new BusinessException(ErrorCode.MAX_RETRY_EXCEEDED, "sessionId = " + req.getSessionId());
        }

//        boolean isValid = otpService.verifyOtp(req.getOtp(), session.getOtpHash(), session.getOtpSalt());
//        if(!isValid){
//            session.incrementRetryCount();
//
//            if (session.isMaxRetryExceeded(MAX_OTP_RETRY)) {
//                session.setStatus(CertSessionStatus.OTP_FAILED);
//            }
//            session.setUpdatedAt(LocalDateTime.now());
//            sessionRepo.save(session);
//
//            int remaining = Math.max(0, MAX_OTP_RETRY - session.getRetryCount());
//            log.warn("[CertificateServiceImpl][verifyOtp] Invalid OTP for sessionId: {}. actualRemaining: {}",
//                    req.getSessionId(), MAX_OTP_RETRY - session.getRetryCount());
//            throw new BusinessException(ErrorCode.INVALID_OTP, "Attempts remaining=" + remaining);
//        }

        // Verify OTP — if incorrect, increment retry and calculate retry directly.
        if (!session.getOtp().equals(req.getOtp())) {
            session.incrementRetryCount();

            if (session.isMaxRetryExceeded(MAX_OTP_RETRY)) {
                session.setStatus(CertSessionStatus.OTP_FAILED);
            }
            session.setUpdatedAt(LocalDateTime.now());
            sessionRepo.save(session);

            int remaining = Math.max(0, MAX_OTP_RETRY - session.getRetryCount());
            log.warn("[CertificateServiceImpl][verifyOtp] Invalid OTP for sessionId: {}. actualRemaining: {}",
                    req.getSessionId(), MAX_OTP_RETRY - session.getRetryCount());
            throw new BusinessException(ErrorCode.INVALID_OTP, "Attempts remaining=" + remaining);
        }

        // Lock link session (prevent replay)
        session.setStatus(CertSessionStatus.OTP_VERIFIED);
        sessionRepo.save(session);


        /** Cmt by Pheim (13/04/2026)
         * Only call generateAsync AFTER the transaction verifyOtp COMMIT is successful.

         * If you call it directly: certAsyncService.generateAsync(sessionId);
         * => then async might run when:
         * 1. the transaction verifyOtp is not committed
         * 2. or is rolled back

         * USE afterCommit
         * verifyOtp()
         *    ↓
         * DB commit successful
         *    ↓
         * afterCommit() trigger
         *    ↓
         * @Async run
         */
        /**
         * It only changes the timing of the call, not the async nature of the call.
         * Can use Spring event (@TransactionalEventListener(phase = AFTER_COMMIT)), message queue (Kafka/RabbitMQ) in the future
         */
        // Trigger async
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        certAsyncService.generateAsync(session.getSessionId());
                    }
                }
        );
        log.debug("END [CertificateServiceImpl][verifyOtp] {}", System.currentTimeMillis());
    }

    @Override
    public CertificateStatusResponse getCertificateStatus(String sessionId) {
        log.info("[CertificateServiceImpl][getCertificateStatus] Start. sessionId={}", sessionId);

        CertificateSession session = getSession(sessionId);
        // CASE 1: Not yet generated => retryAfter
        if (!CertSessionStatus.COMPLETED.equals(session.getStatus())) {

            int retryAfter = switch (session.getStatus()) {
                case CertSessionStatus.OTP_VERIFIED -> 1;
                case CertSessionStatus.PROCESSING -> 2;
                case CertSessionStatus.FAILED -> 5; // optional
                default -> 3;
            };

            return CertificateStatusResponse.builder()
                    .sessionId(sessionId)
                    .status(session.getStatus())
                    .retryAfter(retryAfter) // Suggest that FE call back after dynamic seconds
                    .build();
        }
        // CASE 2: generated => return full data
        Certificate cert = certRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "Certificate not found", "sessionId=" + sessionId));

        return CertificateStatusResponse.builder()
                .sessionId(sessionId)
                .certId(cert.getCertId())
                .status(session.getStatus())
                .fileUrl(cert.getFileUrl())
                .retryAfter(null) // no longer needed
                .build();
    }

    /** Cmt by Phelim (10.04.2026)
     * value = "publicCert" => primary cache for public page
     * key = "'cert:' + #certId" => avoids collisions (later Redis scaling)
     * sync = true, avoids: 1000 requests => MISS => runs RSA 1000 times => only 1 thread loads, the rest wait
     * Cache is here because this method: reading the file (Files.readAllBytes) is I/O heavy, verifying RSA is CPU heavy, calling getTrustScore() + getTimeline() => this is an aggregation endpoint => extremely heavy
     */
    @Override
    @Cacheable(value = BaseConstants.PUBLIC_CERT,
            key = "'cert:' + #certId",
            sync = true
    )
    public PublicCertificateResponse getPublicCertificate(String certId) {
        log.info("[CertificateServiceImpl][getPublicCertificate] Start. certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> {
                    log.warn("[CertificateServiceImpl][getPublicCertificate] Not found certId={}", certId);
                    return new BusinessException(ErrorCode.DATA_NOT_FOUND, "certId=" + certId);
                });

        // 1. VERIFY (AUTO RSA)
        boolean valid = false;
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(cert.getFileUrl()));
            valid = rsaSignatureService.verify(fileBytes, cert.getSignature());
        } catch (Exception e) {
            log.warn("[CertificateServiceImpl][getPublicCertificate] Verify failed certId={}", certId, e);
        }
        String status = valid ? BaseConstants.VALID : BaseConstants.TAMPERED;

        // 2. STORY (CURRENT)
        String sessionId = cert.getSessionId();
        String story = loveStoryRepository.findBySessionIdAndActiveTrue(sessionId)
                .map(LoveStory::getContent)
                .orElse(null);

        // 3. STORY HISTORY
        List<LoveStoryResponse> storyHistory = loveStoryRepository
                .findBySessionIdOrderByVersionDesc(sessionId)
                .stream()
                .map(s -> LoveStoryResponse.builder()
                        .sessionId(s.getSessionId())
                        .content(s.getContent())
                        .version(s.getVersion())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .toList();

        // 4. TRUST SCORE
        CertificateTrustScoreResponse trust = trustService.getTrustScore(certId);

        // 5. TIMELINE
        CertificateTimelineResponse timeline = timelineService.getTimeline(certId);
        return PublicCertificateResponse.builder()
                .certId(certId)
                .valid(valid)
                .status(status)
                .loveStory(story)
                .trustScore(trust.getScore())
                .trustLevel(trust.getLevel().name())
                .revoked(Boolean.TRUE.equals(cert.getRevoked()))
                .storyHistory(storyHistory)
                .timeline(timeline)
                .build();
    }

    // HASH VERIFY
    @Override
    public VerifyPdfResponse verifyByHash(String certId, HttpServletRequest request) {
        log.info("[CertificateVerifyService][verifyByHash] Start certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> {
                    log.warn("[CertificateVerifyService][verifyByHash] Not found certId={}", certId);
                    return new BusinessException(ErrorCode.SESSION_NOT_FOUND, "certId=" + certId);
                });

        // 1. Check revoked
        boolean isRevoked = Boolean.TRUE.equals(cert.getRevoked());
        if (isRevoked) {
            return buildRevokedResponse(certId, VerifyType.HASH, request);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(cert.getFileUrl()));

            String actualHash = hashSignatureService.hash(fileBytes);
            boolean valid = actualHash.equals(cert.getFileHash());

            // log
            verifyLogService.logVerify(certId, BaseConstants.HASH, valid, request);

            return VerifyPdfResponse.builder()
                    .certId(certId)
                    .method(VerifyType.HASH)
                    .status(valid ? BaseConstants.VALID : BaseConstants.TAMPERED)
                    .valid(valid)
                    .expectedHash(cert.getFileHash())
                    .actualHash(actualHash)
                    .verifiedAt(LocalDateTime.now())
                    .build();

        } catch (IOException ex) {
            log.error("[CertificateVerifyService][verifyByHash] File read failed certId={}", certId, ex);
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, "certId=" + certId);
        }
    }

    // RSA VERIFY
    @Override
    public VerifyPdfResponse verifyPdfByRsaSignature(String certId, HttpServletRequest request) {
        log.info("[CertificateVerifyService][verifyPdfByRsaSignature] certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> {
                    log.warn("[CertificateVerifyService][verifyPdfByRsaSignature] Not found certId={}", certId);
                    return new BusinessException(ErrorCode.SESSION_NOT_FOUND, "certId=" + certId);
                });

        // 1. Check revoked
        boolean isRevoked = Boolean.TRUE.equals(cert.getRevoked());
        if (isRevoked) {
            return buildRevokedResponse(certId, VerifyType.RSA, request);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(cert.getFileUrl()));

            boolean valid = rsaSignatureService.verify(fileBytes, cert.getSignature());

            // log
            verifyLogService.logVerify(certId, BaseConstants.RSA, valid, request);

            return VerifyPdfResponse.builder()
                    .certId(certId)
                    .method(VerifyType.RSA)
                    .status(valid ? BaseConstants.VALID : BaseConstants.TAMPERED)
                    .valid(valid)
                    .signature(cert.getSignature())
                    .verifiedAt(LocalDateTime.now())
                    .build();

        } catch (IOException ex) {
            log.error("[CertificateVerifyService][verifyPdfByRsaSignature] File read failed certId={}", certId, ex);
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, "certId=" + certId);
        }
    }

    /** Cmt by Phelim (10.04.2026)
     * The trustScore key may have a different prefix
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = "publicCert", key = "'cert:' + #request.certId"),
            @CacheEvict(value = "trustScore", key = "'trust:' + #request.certId")
    })
    @Transactional
    public CertificateRevokeResponse revoke(CertificateRevokeRequest request) {
        log.info("[CertificateRevocationService][revoke] requestId={}, certId={}",
                request.getRequestId(), request.getCertId());

        Certificate cert = certRepo.findById(request.getCertId())
                .orElseThrow(() -> {
                    log.warn("[CertificateRevocationService][revoke] Not found certId={}", request.getCertId());
                    return new BusinessException(ErrorCode.DATA_NOT_FOUND, "certId=" + request.getCertId());
                });

        // Idempotent
        if (Boolean.TRUE.equals(cert.getRevoked())) {

            log.info("[CertificateRevocationService][revoke] Already revoked certId={}", cert.getCertId());

            return CertificateRevokeResponse.builder()
                    .requestId(request.getRequestId())
                    .certId(cert.getCertId())
                    .status(BaseConstants.REVOKED)
                    .revokedAt(cert.getRevokedAt())
                    .message(BaseConstants.MSG_ALREADY_REVOKED)
                    .build();
        }

        // BUSINESS
        cert.setRevoked(true);
        cert.setRevokedAt(LocalDateTime.now());
        cert.setRevokedReason(request.getReason());

        // JPA auto flush
        log.info("[CertificateRevocationService][revoke] Success certId={}", cert.getCertId());

        return CertificateRevokeResponse.builder()
                .requestId(request.getRequestId())
                .certId(cert.getCertId())
                .status(BaseConstants.REVOKED)
                .revokedAt(cert.getRevokedAt())
                .message(BaseConstants.MSG_REVOKED_SUCCESSFULLY)
                .build();
    }

    @Override
    public CertificateVerifyHistoryResponse getVerifyCertificateHistory(String certId) {
        log.info("[CertificateVerifyHistoryService][getVerifyCertificateHistory] certId={}", certId);

        List<CertificateVerifyLog> logs = cerVerifyLogRepository.findByCertIdOrderByVerifiedAtDesc(certId);

        List<CertificateVerifyHistoryResponse.VerifyLogItem> items = logs.stream()
                .map(log -> CertificateVerifyHistoryResponse.VerifyLogItem.builder()
                        .method(log.getMethod())
                        .result(log.getResult())
                        .ip(LoveCertificateUtil.maskIp(log.getIpAddress()))
                        .userAgent(log.getUserAgent())
                        .verifiedAt(log.getVerifiedAt())
                        .build())
                .toList();

        return CertificateVerifyHistoryResponse.builder()
                .certId(certId)
                .total(items.size())
                .logs(items)
                .build();
    }

    @Override
    public CertificateVerifySummaryResponse getVerifySummary(String certId) {
        log.info("[CertificateServiceImpl][getVerifySummary] certId={}", certId);

        List<CertificateVerifyLog> logs = cerVerifyLogRepository.findByCertIdOrderByVerifiedAtDesc(certId);

        int total = logs.size();
        LocalDateTime lastVerifiedAt = logs.isEmpty()
                ? null
                : logs.getFirst().getVerifiedAt();

        //Retrieve the final status
        String status = logs.isEmpty()
                ? BaseConstants.UNKNOWN_UPPER
                : logs.getFirst().getResult();

        return CertificateVerifySummaryResponse.builder()
                .certId(certId)
                .total(total)
                .lastVerifiedAt(lastVerifiedAt)
                .status(status)
                .build();
    }

    /** Cmt by Phelim (10.04.2026)
     * key available param => @CacheEvict
     *  key must query DB => CacheManager manual
     */
    @Override
    public LoveStoryResponse upsertLoveStory(LoveStoryRequest request) {
        log.info("[CertificateService][upsertLoveStory] Start. sessionId={}", request.getSessionId());

        // Validation + sanitize
        String content = request.getContent();
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Story is empty", null);
        }

        if (content.length() > 2000) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Story too long", null);
        }
        content = HtmlUtils.htmlEscape(content);
        request.setContent(content);

        CertificateSession session = sessionRepo.findById(request.getSessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND, "sessionId=" + request.getSessionId()));

        Optional<Certificate> certOpt = certRepo.findBySessionId(request.getSessionId());
        if (certOpt.isPresent() && Boolean.TRUE.equals(certOpt.get().getRevoked())) {
            log.warn("[CertificateService][upsertLoveStory] Cannot update. Certificate revoked certId={}", certOpt.get().getCertId());
            throw new BusinessException(ErrorCode.CERTIFICATE_REVOKED, "certId=" + certOpt.get().getCertId());
        }

        String certId = certOpt.map(Certificate::getCertId).orElse(null);

        Optional<LoveStory> existingOpt =
                loveStoryRepository.findBySessionIdAndActiveTrue(request.getSessionId());

        LoveStory story;
        if (existingOpt.isPresent()) {
            // Update version
            LoveStory existing = existingOpt.get();
            existing.setActive(false);
            existing.setUpdatedAt(LocalDateTime.now());
            loveStoryRepository.save(existing);

            story = LoveStory.builder()
                    .storyId(generateStoryId())
                    .sessionId(request.getSessionId())
                    .content(request.getContent())
                    .version(existing.getVersion() + 1)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

        } else {
            // Create new story
            story = LoveStory.builder()
                    .storyId(generateStoryId())
                    .sessionId(request.getSessionId())
                    .content(request.getContent())
                    .version(1)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        loveStoryRepository.save(story);

        // Cache evict
        if (certId != null) {
            log.info("[CertificateServiceImpl][upsertLoveStory] Evict cache publicCert certId={}", certId);
            Cache cache = cacheManager.getCache("publicCert");
            if (cache != null) {
                cache.evict("cert:" + certId); // Match with @Cacheable(key = "'cert:' + #certId")
            }
        }

        return LoveStoryResponse.builder()
                .sessionId(story.getSessionId())
                .content(story.getContent())
                .version(story.getVersion())
                .updatedAt(story.getUpdatedAt())
                .build();
    }

    @Override
    public List<LoveStoryResponse> getLoveStoryHistory(String sessionId) {
        log.info("[CertificateServiceImpl][getLoveStoryHistory] Start. sessionId={}", sessionId);
        List<LoveStory> stories = loveStoryRepository.findBySessionIdOrderByVersionDesc(sessionId);

        return stories.stream()
                .map(s -> LoveStoryResponse.builder()
                        .sessionId(s.getSessionId())
                        .content(s.getContent())
                        .version(s.getVersion())
                        .updatedAt(s.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public LoveStoryResponse getLoveStory(String sessionId) {
        log.info("[CertificateServiceImpl][getLoveStory] Start. sessionId={}", sessionId);
        LoveStory story = loveStoryRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "Story not found", "sessionId=" + sessionId));

        return LoveStoryResponse.builder()
                .sessionId(sessionId)
                .content(story.getContent())
                .version(story.getVersion())
                .updatedAt(story.getUpdatedAt())
                .build();
    }

    @Override
    public Resource downloadCertificateStream(String certId) {
        log.info("[CertificateServiceImpl][downloadStream] certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "Certificate not found", "certId=" + certId));

        if (Boolean.TRUE.equals(cert.getRevoked())) {
            throw new BusinessException(ErrorCode.CERTIFICATE_REVOKED, "certId=" + certId);
        }

        try {
            Path path = Path.of(cert.getFileUrl());
            return new InputStreamResource(Files.newInputStream(path));

        } catch (IOException e) {
            log.error("[CertificateServiceImpl][downloadStream] error certId={}", certId, e);
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, "certId=" + certId);
        }
    }

    // =========================
    // COMMON METHODS
    // =========================
    private boolean isSameRequest(CertificateSession session, InitRequest req) {
        return Objects.equals(session.getMaleName(), req.getMaleName())
                && Objects.equals(session.getFemaleName(), req.getFemaleName())
                && Objects.equals(session.getLoveStartDate(), req.getLoveStartDate());
    }

    private String generateSessionId() {
        return BaseConstants.PREFIX_SESSION_ID + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateStoryId() {
        return BaseConstants.PREFIX_LOVE_STORY_ID + System.currentTimeMillis();
    }

    private VerifyPdfResponse buildRevokedResponse(String certId, VerifyType type, HttpServletRequest request) {
        log.warn("[CertificateServiceImpl][buildRevokedResponse] Certificate revoked certId={}", certId);

        verifyLogService.logVerify(certId, type == VerifyType.HASH ? BaseConstants.HASH : BaseConstants.RSA,
                false,
                request);
        return VerifyPdfResponse.builder()
                .certId(certId)
                .method(type)
                .status(BaseConstants.TAMPERED)
                .valid(false)
                .verifiedAt(LocalDateTime.now())
                .build();
    }

    private CertificateSession getSession(String sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND, "sessionId=" + sessionId));
    }

    private CertificateSession getSessionForUpdate(String sessionId) {
        return sessionRepo.findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND, "sessionId=" + sessionId));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        // In case a proxy is used (nginx, gateway)
        return xfHeader.split(",")[0];
    }
}