package com.phelim.system.love_certificate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    VALIDATION_ERROR("LC400", "Invalid request"),
    INVALID_REQUEST("LC400", "Invalid request"),

    SESSION_NOT_FOUND("LC404", "Session not found"),

    IDEMPOTENCY_KEY_REUSED("LC409", "Request reused"),

    INTERNAL_ERROR("LC500", "Internal error"),
    QR_GENERATION_FAILED("LC501", "QR generation failed"),
    TEMPLATE_RENDER_FAILED("LC502", "Template render failed"),
    INVALID_STATE("LC502", "Invalid the Certificate Session's state"),

    SIGNATURE_VERIFY_FAILED("LC502", "Failed to verify signature"),
    SIGNATURE_SIGN_FAILED("LC502", "Failed to sign data"),
    PDF_GENERATION_FAILED("LC502", "PDF generation failed"),
    HASHING_FAILED("LC502", "Hashing failed"),
    SAVING_FILE_FAILED("LC502", "File save failed"),
    RSA_KEYS_NOT_INITIALIZED("LC502", "RSA keys are not initialized"),

    FILE_READ_FAILED("LC503", "File read failed"),
    LOAD_KEY_FAILED("LC504", "Load key failed"),
    DATA_NOT_FOUND("LC505", "Not found certificate"),

    OTP_EXPIRED("LC505", "OTP has expired"),
    INVALID_OTP("LC505", "Invalid OTP"),

    OTP_SEND_FAILED("SMS001","Unable to send OTP via SMS. Please try again later."),
    EMAIL_SEND_FAILED("SMS001","Unable to send message via email. Please try again later."),

    TIMEOUT_ERROR("504", "Hết thời gian chờ xử lý (Timeout)"),
    FEIGN_CLIENT_BUSINESS_ERROR("FC111", "FeignClient service returned business error"),
    FEIGN_CLIENT_DETAIL_MESSAGE_ERROR_FOR_NOT_OK("FC112","Detail error message for 01 code from Audit Service"),
    FEIGN_CLIENT_NOT_FOUND("WL003", "Feign client not found"),
    DOWNSTREAM_SERVICE_UNAVAILABLE("WL003", "Downstream service is unavailable or returned an internal error"),
    FEIGN_CLIENT_UNAVAILABLE("503", "Dịch vụ tạm thời không khả dụng"),
    FEIGN_CLIENT_INVALID_RESPONSE("FC110", "Invalid response from FeignClient service"),

    CERTIFICATE_REVOKED("FC110", "Cannot update love story after revoke");


    private final String code;
    private final String message;
}