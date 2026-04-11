package com.phelim.system.love_certificate.dto.response;
import com.phelim.system.love_certificate.enums.LevelTrust;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTrustScoreResponse {

    private String certId;

    private int score;

    private LevelTrust level;

    private boolean revoked;

    private int totalVerify;

    private int uniqueUsers;

    private LocalDateTime lastVerifiedAt;

    private FraudInfo fraud;

    @Getter
    @Setter
    @Builder
    public static class FraudInfo {
        private boolean spam;
        private boolean bot;
        private boolean suspicious;
    }
}
