package com.phelim.system.love_certificate.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateVerifySummaryResponse {

    private String certId;

    private int total;

    private LocalDateTime lastVerifiedAt;

    private String status; // VALID / INVALID
}
