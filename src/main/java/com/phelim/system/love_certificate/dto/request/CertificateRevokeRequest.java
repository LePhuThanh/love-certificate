package com.phelim.system.love_certificate.dto.request;

import com.phelim.system.love_certificate.dto.HasRequestId;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRevokeRequest implements HasRequestId {

    @NotBlank(message = "requestId is required")
    private String requestId;

    @NotBlank(message = "certId is required")
    private String certId;

    @NotBlank(message = "reason is required")
    private String reason;
}