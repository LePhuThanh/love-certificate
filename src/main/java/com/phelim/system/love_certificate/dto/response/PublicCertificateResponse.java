package com.phelim.system.love_certificate.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicCertificateResponse {

    private String certId;

    private boolean qrValid;

    // Verify
    private boolean valid;
    private String status; // VALID / TAMPERED

    // Story
    private String loveStory;

    // Trust
    private int trustScore;
    private String trustLevel;

    private boolean revoked;
    private List<LoveStoryResponse> storyHistory;

    // Timeline
    private CertificateTimelineResponse timeline;
}
