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

    // 🔥 VERIFY
    private boolean valid;
    private String status; // VALID / TAMPERED

    // 🔥 STORY
    private String loveStory;

    // 🔥 TRUST
    private int trustScore;
    private String trustLevel;

    private boolean revoked;
    private List<LoveStoryResponse> storyHistory;

    // 🔥 TIMELINE
    private CertificateTimelineResponse timeline;
}
