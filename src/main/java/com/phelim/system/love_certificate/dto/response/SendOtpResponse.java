package com.phelim.system.love_certificate.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendOtpResponse {

    private String sessionId;
    private int otpExpireIn;
    private String status; // OTP_SENT
}
