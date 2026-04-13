package com.phelim.system.love_certificate.dto.request;

import com.phelim.system.love_certificate.dto.HasRequestId;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewCertRequest implements HasRequestId {
    @NotBlank(message = "requestId is required")
    private String requestId;
    @NotBlank(message = "sessionId is required")
    private String sessionId;
}