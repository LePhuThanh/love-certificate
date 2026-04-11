package com.phelim.system.love_certificate.controller;

import com.phelim.system.love_certificate.config.LoveCertificateProperties;
import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.feignclient.LoveCertificateResponse;
import com.phelim.system.love_certificate.dto.request.*;
import com.phelim.system.love_certificate.dto.response.*;
import com.phelim.system.love_certificate.entity.CertificateSession;
import com.phelim.system.love_certificate.service.application.CertificateApplicationService;
import com.phelim.system.love_certificate.service.application.CertificateService;
import com.phelim.system.love_certificate.util.RequestIdValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/core/love-certificates")
@RequiredArgsConstructor
@Slf4j
public class CertificateController {

    private final CertificateService certificateService;
    private final LoveCertificateProperties loveCertificateProperties;
    private final CertificateApplicationService appService;

    @PostMapping("/v1/init")
    public LoveCertificateResponse<InitResponse> init(@Valid @RequestBody InitRequest req, HttpServletRequest httpRequest) {
//        httpRequest.setAttribute(BaseConstants.REQUEST_ID, req.getRequestId());

        RequestIdValidator.sync(httpRequest, req);
        CertificateSession session = certificateService.init(req);
        return LoveCertificateResponse.success(
                req.getRequestId(),
                InitResponse.builder()
                        .sessionId(session.getSessionId())
                        .status(session.getStatus())
                        .createdAt(session.getCreatedAt().toString())
                        .build());
    }

    @PostMapping("/v1/preview")
    public LoveCertificateResponse<PreviewResponse> preview(@Valid @RequestBody GenerateRequest req, HttpServletRequest httpRequest) {

        RequestIdValidator.sync(httpRequest, req);
        byte[] pdf = certificateService.previewCert(req);

        String base64 = Base64.getEncoder().encodeToString(pdf);
        return LoveCertificateResponse.success(
                req.getRequestId(),
                PreviewResponse.builder()
                        .sessionId(req.getSessionId())
                        .fileBase64(base64)
                        .fileName(BaseConstants.CERTIFICATE_NAME_PREVIEW)
                        .build());
    }

    @PostMapping("/v1/sessions/upsert-story")
    public LoveCertificateResponse<LoveStoryResponse> upsertLoveStory(@Valid @RequestBody LoveStoryRequest request,
                                                                      HttpServletRequest httpRequest) {
        RequestIdValidator.sync(httpRequest, request);
        request.setSessionId(request.getSessionId());
        LoveStoryResponse res = certificateService.upsertLoveStory(request);

        return LoveCertificateResponse.success(request.getRequestId(), res);
    }

    @PostMapping("/v1/send-otp")
    public LoveCertificateResponse<SendOtpResponse> sendOtp(@Valid @RequestBody GenerateRequest req, HttpServletRequest httpRequest) {

        RequestIdValidator.sync(httpRequest, req);
        certificateService.sendOtp(req);

        return LoveCertificateResponse.success(
                req.getRequestId(),
                SendOtpResponse.builder()
                        .sessionId(req.getSessionId())
                        .otpExpireIn(loveCertificateProperties.getOtpExpireSeconds())
                        .status(BaseConstants.OTP_SENT)
                        .build());
    }

    @PostMapping("/v1/verify-otp")
    public LoveCertificateResponse<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest req, HttpServletRequest httpRequest) {

        RequestIdValidator.sync(httpRequest, req);
        certificateService.verifyOtp(req);

        return LoveCertificateResponse.success(
                req.getRequestId(),
                VerifyOtpResponse.builder()
                        .sessionId(req.getSessionId())
                        .status(BaseConstants.VERIFIED)
                        .build());
    }

    @GetMapping("/v1/status/{sessionId}")
    public LoveCertificateResponse<CertificateStatusResponse> getCertificateStatus(@PathVariable(name = "sessionId") String sessionId,
                                                                                   HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);
        CertificateStatusResponse res = certificateService.getCertificateStatus(sessionId);

        return LoveCertificateResponse.success(requestId, res);
    }

    @GetMapping("/v1/certificates/{certId}/download")
    public ResponseEntity<?> downloadCertificate(@PathVariable(name = "certId") String certId,
                                                 HttpServletRequest request) {
        String requestId = (String) request.getAttribute(BaseConstants.REQUEST_ID);
        Resource resource = certificateService.downloadCertificateStream(certId);

        return ResponseEntity.ok()
                .header("X-Request-Id", requestId)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + certId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/v1/public/cert/{certId}")
    public LoveCertificateResponse<PublicCertificateResponse> getPublicCertificate(@PathVariable(name = "certId") String certId,
                                                                                   HttpServletRequest request) {
        String requestId = (String) request.getAttribute(BaseConstants.REQUEST_ID);
        PublicCertificateResponse res = certificateService.getPublicCertificate(certId);

        return LoveCertificateResponse.success(requestId, res);
    }

    // đọc file từ disk
    // hash lại
    // compare DB
    // detect tamper
    //PDF VERIFY HASH (legacy)

    //verify + revoke check
    //verify + audit log
    //track user behavior (IP, UA)
    @GetMapping("/v1/verify-pdf/hash/{certId}")
    public LoveCertificateResponse<VerifyPdfResponse> verifyPdfByHash(@PathVariable(name = "certId") String certId,
                                                                      HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);
        VerifyPdfResponse res = certificateService.verifyByHash(certId, httpRequest);

        return LoveCertificateResponse.success(requestId, res);
    }

    //PDF VERIFY RSA (new)
    @GetMapping("/v1/verify-pdf/signature/{certId}")
    public LoveCertificateResponse<VerifyPdfResponse> verifyPdfByRsaSignature(@PathVariable(name = "certId") String certId,
                                                                              HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);
        VerifyPdfResponse res = certificateService.verifyPdfByRsaSignature(certId, httpRequest);

        return LoveCertificateResponse.success(requestId, res);
    }

    @GetMapping("/v1/{certId}/verify-summary")
    public LoveCertificateResponse<CertificateVerifySummaryResponse> getVerifySummary(@PathVariable(name = "certId") String certId,
                                                                                      HttpServletRequest httpRequest) {
        CertificateVerifySummaryResponse res = certificateService.getVerifySummary(certId);
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);

        return LoveCertificateResponse.success(requestId, res);
    }

    @GetMapping("/v1/admin/{certId}/verify-history")
    public LoveCertificateResponse<CertificateVerifyHistoryResponse> getVerifyCertificateHistory(@PathVariable(name = "certId") String certId,
                                                                                                 HttpServletRequest httpRequest) {
        CertificateVerifyHistoryResponse res = certificateService.getVerifyCertificateHistory(certId);
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);

        return LoveCertificateResponse.success(requestId, res);
    }

    @PostMapping("/v1/revoke")
    public LoveCertificateResponse<CertificateRevokeResponse> revoke(@Valid @RequestBody CertificateRevokeRequest request,
                                                                     HttpServletRequest httpRequest) {
        RequestIdValidator.sync(httpRequest, request);
        CertificateRevokeResponse res = certificateService.revoke(request);

        return LoveCertificateResponse.success(request.getRequestId(), res);
    }

    @GetMapping("/v1/{certId}/trust-score")
    public LoveCertificateResponse<CertificateTrustScoreResponse> getTrustScore(@PathVariable(name = "certId") String certId,
                                                                                HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);
        CertificateTrustScoreResponse res = appService.getTrustScore(certId);

        return LoveCertificateResponse.success(requestId, res);
    }

    @GetMapping("/v1/{certId}/timeline")
    public LoveCertificateResponse<CertificateTimelineResponse> getTimeline(@PathVariable(name = "certId") String certId,
                                                                            HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);
        CertificateTimelineResponse res = appService.getTimeline(certId);

        return LoveCertificateResponse.success(requestId, res);
    }

    @GetMapping("/v1/{sessionId}/story")
    public LoveCertificateResponse<LoveStoryResponse> getLoveStory(@PathVariable(name = "sessionId") String sessionId,
                                                                   HttpServletRequest httpRequest) {
        String requestId = (String) httpRequest.getAttribute(BaseConstants.REQUEST_ID);
        LoveStoryResponse res = certificateService.getLoveStory(sessionId);

        return LoveCertificateResponse.success(requestId, res);
    }

    @GetMapping("/v1/sessions/{sessionId}/story/history")
    public LoveCertificateResponse<List<LoveStoryResponse>> getLoveStoryHistory(@PathVariable(name = "sessionId") String sessionId,
                                                                                HttpServletRequest request) {
        String requestId = (String) request.getAttribute(BaseConstants.REQUEST_ID);
        List<LoveStoryResponse> res = certificateService.getLoveStoryHistory(sessionId);

        return LoveCertificateResponse.success(requestId, res);
    }

}
