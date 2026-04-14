package com.phelim.system.love_certificate.dto.response;

import com.phelim.system.love_certificate.enums.CertSessionStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateStatusResponse {

    private String certId;
    private String sessionId;
    private CertSessionStatus status;

    private String fileUrl; // When COMPLETED

    private Integer retryAfter; // seconds // for polling API (FE)
}
