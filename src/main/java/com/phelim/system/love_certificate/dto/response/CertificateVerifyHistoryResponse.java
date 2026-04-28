package com.phelim.system.love_certificate.dto.response;

import com.phelim.system.love_certificate.enums.VerificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateVerifyHistoryResponse {

    private String certId;

    private int total; // total of verify

    private List<VerifyLogItem> logs;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyLogItem {

        private VerificationType method;      // HASH | RSA | QR
        private String result;      // VALID / INVALID
        private String ip;          // IP user
        private String userAgent;   // browser / device
        private LocalDateTime verifiedAt;
    }
}
