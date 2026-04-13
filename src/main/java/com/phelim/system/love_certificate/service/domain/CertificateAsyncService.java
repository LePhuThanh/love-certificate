package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.constant.CertSessionStatus;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.CertificateSession;
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


    /** Cmt by Phelim (13/04/2026)
     *  => Flow generate certificate (final design)

     *      1. Render HTML draft (QR placeholder)
     *      2. Generate draft PDF (ensure layout stable)
     *      3. Build verify URL (no hash, server-side verification)
     *      4. Render final HTML with QR (verify endpoint)
     *      5. Generate final PDF
     *      6. Compute SHA-256 hash of final PDF (integrity)
     *      7. Persist file + hash

     *      => Design note:
     *      - QR only contains certId (no hash)
     *      - Verification is server-driven (recompute hash from stored file)
     *      - Avoids double-render dependency between QR and hash

     *      => Security model:
     *      - File integrity is guaranteed by SHA-256 hash
     *      - Hash is computed from final persisted PDF
     *      - Any modification of PDF will break hash equality
     */
    @Async(BaseConstants.ASYNC_NAME)
    @Transactional
    public void generateAsync(String sessionId) {
        log.info("[CertificateAsyncService][generateAsync] Start. sessionId={}", sessionId);
        log.debug("-------------------------------Thread generateAsync: {}", Thread.currentThread().getName());

        // LOCK AGAIN (defense against duplicate async trigger)
        CertificateSession session = getSessionForUpdate(sessionId);

        // Idempotent
        if (CertSessionStatus.COMPLETED.equals(session.getStatus())) {
            log.info("[generateAsync] already completed sessionId={}", sessionId);
            return;
        }

        if (CertSessionStatus.PROCESSING.equals(session.getStatus())) {
            log.warn("[generateAsync] already processing sessionId={}", sessionId);
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

            // STEP 2: build verifyUrl
            String verifyUrl = "http://localhost:8080/core/love-certificates/v1/public/cert/" + certId;

            // STEP 3: re-render HTML FINAL + generate PDF FINAL
            String finalHtml = templateService.renderCertificate(cert, session, verifyUrl);
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
            log.error("[CertificateAsyncService][generateAsync] failed sessionId={}", sessionId, ex);
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
