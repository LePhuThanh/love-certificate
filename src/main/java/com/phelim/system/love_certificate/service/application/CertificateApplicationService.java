package com.phelim.system.love_certificate.service.application;

import com.phelim.system.love_certificate.dto.response.CertificateTimelineResponse;
import com.phelim.system.love_certificate.dto.response.CertificateTrustScoreResponse;
import com.phelim.system.love_certificate.service.domain.RsaSignatureService;
import com.phelim.system.love_certificate.service.domain.TimelineService;
import com.phelim.system.love_certificate.service.domain.TrustService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


// Orchestrator (NO CACHE)
@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateApplicationService {

    private final TrustService trustService;
    private final TimelineService timelineService;
    private final RsaSignatureService rsaSignatureService;

    public CertificateTrustScoreResponse getTrustScore(String certId) {
        return trustService.getTrustScore(certId);
    }

    public CertificateTimelineResponse getTimeline(String certId) {
        return timelineService.getTimeline(certId);
    }
}
