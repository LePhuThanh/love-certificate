package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.CertificateSession;
import com.phelim.system.love_certificate.enums.CertSessionStatus;
import com.phelim.system.love_certificate.enums.CertificateType;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.repository.CertificateRepository;
import com.phelim.system.love_certificate.repository.CertificateSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateAsyncService {

    private final CertificateSessionRepository sessionRepo;
    private final CertificateRepository certRepo;
    private final RuleEngineService ruleEngine;
    private final PdfService pdfService;
    private final TemplateService templateService;
    private final FileStorageService fileStorageService;
    private final HashSignatureService hashSignatureService;
    private final RsaSignatureService rsaSignatureService;


    /** Cmt by Phelim (20/04/2026)
     * => Flow generate certificate (final design)
     * 1. Render HTML draft (QR placeholder)
     * 2. Generate draft PDF (ensure layout stable)
     * 3. Generate STATIC QR (UPDATED)
     *    - QR contains ONLY: /public/cert/{certId}
     *    - No timestamp, no signature embedded in PDF
     * 4. Render final HTML with STATIC QR
     * 5. Generate final PDF
     * 6. Compute SHA-256 hash of final PDF (integrity)
     * 7. RSA sign final PDF (file-level security)
     * 8. Persist file + hash + signature
     * => Runtime verification:
     * - When user scans QR or FE calls API:
     *     + system MAY generate:
     *         qrTimestamp + qrSignature (RSA(certId|timestamp))
     *     + used for:
     *         - anti-fake link (entry security)
     *         - behavior analysis (replay detection)
     * => Security model:
     * - STATIC QR          => stable entry point (UX friendly)
     * - Runtime Signature  => optional entry validation (anti-fake QR)
     * - RSA file signature => document integrity (anti tampering)
     * - Hash               => additional integrity layer
     * => Design note:
     * - QR in PDF NEVER expires
     * - Timestamp is NOT stored, NOT embedded in PDF
     * - Signature is generated dynamically at runtime
     * => Multi-layer security (defense-in-depth):
     * - Entry (QR/link)
     * - Content (PDF)
     * - Behavior (log + trust score)
     */
    @Async(BaseConstants.EXECUTOR_ASYNC_GENERATE_CER)
    @Transactional
    public void generateAsync(String sessionId) {
        log.info("[CertificateAsyncService][generateAsync] Start. sessionId={}", sessionId);
        log.debug("-------------------------------Thread generateAsync: {}", Thread.currentThread().getName());

        // LOCK AGAIN (defense against duplicate async trigger)
        CertificateSession session = getSessionForUpdate(sessionId);

        // Idempotent
        if (CertSessionStatus.COMPLETED.equals(session.getStatus())) {
            log.info("[CertificateAsyncService][generateAsync] Already completed sessionId={}", sessionId);
            return;
        }

        if (CertSessionStatus.PROCESSING.equals(session.getStatus())) {
            log.warn("[CertificateAsyncService][generateAsync] Already processing sessionId={}", sessionId);
            return;
        }

        session.setStatus(CertSessionStatus.PROCESSING);
        sessionRepo.save(session);
        try {
            int days = (int) ChronoUnit.DAYS.between(
                    session.getLoveStartDate(),
                    LocalDate.now()
            );

            CertificateType type = ruleEngine.determineType(days);
            String certId = generateCertId();
            Certificate cert = Certificate.builder()
                    .certId(certId)
                    .sessionId(sessionId)
                    .durationDays(days)
                    .type(type)
                    .issuedAt(LocalDateTime.now())
                    .build();

            // STEP 1: Render HTML draft (QR placeholder) + Generate draft PDF (ensure layout stable)
            String htmlDraft = templateService.renderCertificate(cert, session, BaseConstants.DUMMY);
            byte[] draftPdf = pdfService.generatePdf(htmlDraft);

            // STEP 2: build static QR URL
            String publicCertUrl = "http://localhost:8080/core/love-certificates/v1/public/cert/" + certId;

            // STEP 3: re-render HTML FINAL + generate PDF FINAL
            String finalHtml = templateService.renderCertificate(cert, session, publicCertUrl);
            byte[] finalPdf = pdfService.generatePdf(finalHtml);

            // STEP 4: HASH FINAL PDF
            String hash = hashSignatureService.hash(finalPdf);

            // Add signature (Only VerifyType.RSA)
            String signature = rsaSignatureService.sign(finalPdf);
            cert.setSignature(signature);

            // Save file
            String fileUrl = fileStorageService.savePdfFile(certId + ".pdf", finalPdf);

            cert.setFileHash(hash);
            cert.setFileUrl(fileUrl);

            certRepo.save(cert);
            session.setStatus(CertSessionStatus.COMPLETED);

        } catch (Exception ex) {
            log.error("[CertificateAsyncService][generateAsync] Failed sessionId={}", sessionId, ex);
            session.setStatus(CertSessionStatus.FAILED);
        }
        sessionRepo.save(session);
    }

    private CertificateSession getSessionForUpdate(String sessionId) {
        return sessionRepo.findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND, "sessionId=" + sessionId));
    }

    private String generateCertId() {
        return BaseConstants.PREFIX_CERTIFICATE_ID + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
