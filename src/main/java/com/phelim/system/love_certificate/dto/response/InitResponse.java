package com.phelim.system.love_certificate.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitResponse {

    private String sessionId;
    private String status;
    private String createdAt;
}
