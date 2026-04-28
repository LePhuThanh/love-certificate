package com.phelim.system.love_certificate.service.application;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.response.CertificateTimelineResponse;
import com.phelim.system.love_certificate.dto.response.CertificateTrustScoreResponse;
import com.phelim.system.love_certificate.dto.response.LoveStoryResponse;
import com.phelim.system.love_certificate.dto.response.PublicCertificateResponse;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.LoveStory;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.repository.CertificateRepository;
import com.phelim.system.love_certificate.repository.LoveStoryRepository;
import com.phelim.system.love_certificate.service.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateQueryService {

    private final CertificateRepository certRepo;
    private final RsaSignatureService rsaSignatureService;

    private final LoveStoryRepository loveStoryRepository ;
    private final TrustService trustService;
    private final TimelineService timelineService;

    /** Cmt by Phelim (20/04/2026)
     * value = "publicCert" => primary cache for public page
     * key = "'cert:' + #certId" => avoids collisions
     * sync = true => prevent cache stampede (only 1 thread loads RSA verify)
     */
    @Cacheable(
            value = BaseConstants.PUBLIC_CERT,
            key = "'cert:' + #certId",
            sync = true
    )
    public PublicCertificateResponse getPublicCertificateData(String certId) {
        log.info("[CertificateQueryService][getPublicCertificateData] certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> {
                    log.warn("[CertificateQueryService][getPublicCertificateData] Not found certId={}", certId);
                    return new BusinessException(ErrorCode.DATA_NOT_FOUND, "certId=" + certId);
                });

        // 1. Verify certificate content (auto rsa)
        boolean valid = false;
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(cert.getFileUrl()));
            valid = rsaSignatureService.verify(fileBytes, cert.getSignature());
        } catch (Exception e) {
            log.warn("[CertificateQueryService] Verify failed certId={}", certId, e);
        }
        String status = valid ? BaseConstants.VALID : BaseConstants.TAMPERED;

        // 2. Story (current)
        String sessionId = cert.getSessionId();
        String story = loveStoryRepository.findBySessionIdAndActiveTrue(sessionId)
                .map(LoveStory::getContent)
                .orElse(null);

        // 3. Story history
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
        // 4. Trust score
        CertificateTrustScoreResponse trust = trustService.getTrustScore(certId);

        // 5. Timeline
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
}
