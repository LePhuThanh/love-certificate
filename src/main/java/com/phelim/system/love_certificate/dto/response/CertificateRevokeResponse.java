package com.phelim.system.love_certificate.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRevokeResponse {

    private String requestId;

    private String certId;

    private String status; // REVOKED

    private LocalDateTime revokedAt;

    private String message;
}
