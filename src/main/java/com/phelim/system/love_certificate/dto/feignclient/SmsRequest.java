package com.phelim.system.love_certificate.dto.feignclient;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SmsRequest {
    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;
    @NotBlank(message = "content is required")
    private String content;
    @NotBlank(message = "keyword is required")
    private String keyword;
}
