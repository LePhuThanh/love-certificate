package com.phelim.system.love_certificate.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpResponse {

    private String sessionId;
    private String status; // VERIFIED
}
