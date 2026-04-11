package com.phelim.system.love_certificate.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoveStoryResponse {

    private String sessionId;
    private String content;
    private Integer version;
    private LocalDateTime updatedAt;
}
