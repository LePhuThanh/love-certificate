package com.phelim.system.love_certificate.dto.request;

import com.phelim.system.love_certificate.dto.HasRequestId;
import com.phelim.system.love_certificate.enums.Region;
import com.phelim.system.love_certificate.validation.annotation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRequest implements HasRequestId {
    @NotBlank(message = "requestId is required")
    private String requestId;
    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotBlank(message = "phoneNumber is required")
    @PhoneNumber(allowInternational = true)
    private String phoneNumber;
    @NotNull(message = "region is required")
    private Region region;

}
