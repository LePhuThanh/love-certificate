package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.entity.CertificateVerifyLog;
import com.phelim.system.love_certificate.repository.CertificateVerifyLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyLogService {

    private final CertificateVerifyLogRepository cerVerifyLogRepository;
    private final HashSignatureService hashSignatureService;

    @CacheEvict(
            value = "trustScore",
            key = "'trust:' + #certId"
    )
    public void logVerify(String certId, String method, boolean valid, HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader(BaseConstants.USER_AGENT);

        String ipHash = hashSignatureService.hashString(ip);
        String fingerprint = hashSignatureService.hashString(ip + "|" + userAgent);

        CertificateVerifyLog log = CertificateVerifyLog.builder()
                .certId(certId)
                .method(method)
                .result(valid ? BaseConstants.VALID : BaseConstants.INVALID)
                .ipHash(ipHash)
                .fingerprint(fingerprint)
                .ipAddress(ip) // debug local
                .userAgent(userAgent)
                .verifiedAt(LocalDateTime.now())
                .build();
        cerVerifyLogRepository.save(log);
    }
}
