package com.phelim.system.love_certificate.dto.feignclient;

import com.phelim.system.love_certificate.constant.BaseConstants;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoveCertificateResponse<T> {
    private String requestId;
    private String code;
    private String message;
    private String timestamp;
    private T data;

    public static <T> LoveCertificateResponse<T> success(String requestId,T data) {
        return LoveCertificateResponse.<T>builder()
                .requestId(requestId)
                .code(BaseConstants.SUCCESS_CODE)
                .message(BaseConstants.SUCCESS)
                .timestamp(LocalDateTime.now().toString())
                .data(data)
                .build();
    }

    public static <T> LoveCertificateResponse<T> error(String requestId,String code, String message) {
        return LoveCertificateResponse.<T>builder()
                .requestId(requestId)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .data(null)
                .build();
    }

    public static <T> LoveCertificateResponse<T> error(String code, String message, T data) {
        return LoveCertificateResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
