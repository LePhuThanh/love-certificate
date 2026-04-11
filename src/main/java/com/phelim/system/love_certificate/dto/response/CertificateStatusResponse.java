package com.phelim.system.love_certificate.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateStatusResponse {

    private String certId;
    private String sessionId;
    private String status;

    private String fileUrl; // When COMPLETED

    private Integer retryAfter; // seconds // for polling API (FE)
}
