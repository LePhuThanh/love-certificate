package com.phelim.system.love_certificate.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.phelim.system.love_certificate.enums.VerifyType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyPdfResponse {
    private String certId;

    private VerifyType method; // HASH | RSA
    private String status; // VALID | TAMPERED

    private boolean valid;

    private String expectedHash; // using for HASH
    private String actualHash;   // using for HASH

    private String signature;    // using for RSA

    private LocalDateTime verifiedAt;
}
