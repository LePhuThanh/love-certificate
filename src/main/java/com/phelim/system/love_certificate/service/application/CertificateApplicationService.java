package com.phelim.system.love_certificate.service.application;

import com.phelim.system.love_certificate.dto.response.CertificateTimelineResponse;
import com.phelim.system.love_certificate.dto.response.CertificateTrustScoreResponse;
import com.phelim.system.love_certificate.service.domain.TimelineService;
import com.phelim.system.love_certificate.service.domain.TrustService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateApplicationService {

    private final TrustService trustService;
    private final TimelineService timelineService;

    public CertificateTrustScoreResponse getTrustScore(String certId) {
        return trustService.getTrustScore(certId);
    }

    public CertificateTimelineResponse getTimeline(String certId) {
        return timelineService.getTimeline(certId);
    }
}
