package com.phelim.system.love_certificate.dto.response;

import com.phelim.system.love_certificate.enums.CertSessionStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitResponse {

    private String sessionId;
    private CertSessionStatus status;
    private String createdAt;
}
