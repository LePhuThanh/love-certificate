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
    CIRCUIT_BREAKER_NOT_FOUND("LC505", "Circuit breaker not found"),

    OTP_EXPIRED("LC505", "OTP has expired"),
    INVALID_OTP("LC505", "Invalid OTP"),

    OTP_SEND_FAILED("SMS001","Unable to send OTP via SMS. Please try again later"),
    EMAIL_SEND_FAILED("SMS001","Unable to send message via email. Please try again later"),

    TIMEOUT_ERROR("504", "The request timed out while waiting for a response"),
    FEIGN_CLIENT_BUSINESS_ERROR("FC111", "FeignClient service returned business error"),
    FEIGN_CLIENT_DETAIL_MESSAGE_ERROR_FOR_NOT_OK("FC112","Detail error message for 01 code from Audit Service"),
    FEIGN_CLIENT_NOT_FOUND("WL003", "Feign client not found"),
    DOWNSTREAM_SERVICE_UNAVAILABLE("WL003", "Downstream service is unavailable or returned an internal error"),
    FEIGN_CLIENT_UNAVAILABLE("503", "The service is temporarily unavailable. Please try again later"),
    FEIGN_CLIENT_INVALID_RESPONSE("FC110", "Invalid response from FeignClient service"),

    CERTIFICATE_REVOKED("FC110", "Cannot update love story after revoke"),
    CERTIFICATE_NOT_FOUND("FC110", "Certificate not found"),

    PHONE_NUMBER_INVALID_FORMAT("FC110","Invalid phone number format"),
    VN_PHONE_NUMBER_INVALID_FORMAT("FC110","Invalid phone number format"),
    INTL_PHONE_NUMBER_INVALID_FORMAT("FC110","Invalid phone number format"),

    MAX_RETRY_EXCEEDED("FC110","Exceeded the number of OTP retry."),

    SMS_FAILED("FC110", "Failed to send OTP. Please try again later"),

    IDENTIFICATION_INVALID_FORMAT("FC110", "Identification invalid format"),
    PHONE_MISMATCH("FC110", "Phone number mismatch the core session phone number");

    private final String code;
    private final String message;
}