package com.phelim.system.love_certificate.service.application;

import com.phelim.system.love_certificate.dto.request.*;
import com.phelim.system.love_certificate.dto.response.*;
import com.phelim.system.love_certificate.entity.CertificateSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;

import java.util.List;

public interface CertificateService {
    CertificateSession init(InitRequest req);
    byte[] previewCertificate(PreviewCertRequest req);
    void sendOtp(GenerateRequest req, HttpServletRequest httpRequest);
    void verifyOtp(VerifyOtpRequest req);
    CertificateStatusResponse getCertificateStatus(String sessionId);
    PublicCertificateResponse getPublicCertificate(String certId,
                                                   Long timestamp,
                                                   String signature,
                                                   HttpServletRequest request);

    VerifyPdfResponse verifyByHash(String certId, HttpServletRequest request);
    VerifyPdfResponse verifyPdfByRsaSignature(String certId, HttpServletRequest request);
    CertificateRevokeResponse revoke(CertificateRevokeRequest request);
    CertificateVerifyHistoryResponse getVerifyCertificateHistory(String certId);
    CertificateVerifySummaryResponse getVerifySummary(String certId);

    LoveStoryResponse getLoveStory(String sessionId);
    LoveStoryResponse upsertLoveStory(LoveStoryRequest request);
    List<LoveStoryResponse> getLoveStoryHistory(String sessionId);
    Resource downloadCertificateStream(String certId);

}
