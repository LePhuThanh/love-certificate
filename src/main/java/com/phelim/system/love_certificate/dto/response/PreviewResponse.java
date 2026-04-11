package com.phelim.system.love_certificate.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreviewResponse {

    private String sessionId;
    private String fileName;
    private String fileBase64;
}
