package com.phelim.system.love_certificate.constant;

public final class BaseConstants {

    private BaseConstants() {
        // prevent instantiation
    }

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY = "logId";

    public static final String CERTIFICATE_NAME_HTML_FORMAT = "love_certificate.html";
    public static final String CERTIFICATE_NAME_PREVIEW = "Preview_Certificate.pdf";

    public static final String QR_SCAN_PATH = "http://localhost:8080/api/qr/v1/scan?token=";
    public static final String QR_PAYMENT_SCAN_PATH = "http://localhost:8080/api/v1/qr-payment/scan?token=";
    public static final String PREFIX_SESSION_ID = "CS_";
    public static final String PREFIX_CERTIFICATE_ID = "CERT_";
    public static final String PREFIX_LOVE_STORY_ID = "STORY_";
    public static final String PREFIX_MILESTONE_LOG_ID = "ML_";
    public static final String PREFIX_EMAIL_ID = "EMAIL_";

    public static final String OTP_SENT = "OTP_SENT";
    public static final String VERIFIED = "VERIFIED";

    public static final String HIT_CACHE = "HIT";
    public static final String MISS_CACHE = "MISS";

    public static final String ASYNC_NAME = "asyncTaskExecutor";
    public static final String SCHEDULER_NAME = "schedulerTaskExecutor";

    public static final String HASH = "HASH";
    public static final String USER_AGENT = "User-Agent";
    public static final String UNKNOWN_UPPER = "UNKNOWN";
    public static final String UNKNOWN_CAPITALIZED = "Unknown";

    public static final String ALGORITHM_SHA_256 = "SHA-256";

    public static final String VALID = "VALID";
    public static final String INVALID = "INVALID";

    public static final String TAMPERED = "TAMPERED";
    public static final String REVOKED = "REVOKED";
    public static final String DUMMY = "DUMMY";

    public static final String MSG_ALREADY_REVOKED = "Already revoked";
    public static final String MSG_REVOKED_SUCCESSFULLY = "Certificate revoked successfully";

    public static final String TEMP = "TEMP";

    public static final String ALGORITHM_SHA256_WITH_RSA = "SHA256withRSA";
    public static final String RSA = "RSA";

    public static final String PRIVATE_KEY_PEM = "private_key.pem";
    public static final String PUBLIC_KEY_PEM = "public_key.pem";

    public static final String CACHE_ATTR = "X_CACHE";
    public static final String CACHE_NAME_ATTR = "X_CACHE_NAME";
    public static final String CACHE_KEY_ATTR = "X_CACHE_KEY";


    public static final String KEY_DEFAULT_NAME_CAPITALIZED = "Default";


    public static final String REQUEST_ID = "requestId";
    public static final String GET_REQUEST_ID = "getRequestId";

    public static final String BASE_PATH = "files";
    public static final String HTML_PATH = "html";
    public static final String GENERATED_KEY_BASE_PATH = "src/main/resources/keys/";

    public static final String A_CERTIFICATE_TEMPLATE_NAME = "certificate-a";
    public static final String B_CERTIFICATE_TEMPLATE_NAME = "certificate-b";
    public static final String C_CERTIFICATE_TEMPLATE_NAME = "certificate-c";

    public static final String TEST_ENVIRONMENT = "TEST";
    public static final String LIVE_ENVIRONMENT = "LIVE";
    public static final String PBKDF2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256";

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String DEAD = "DEAD";
    public static final String SENT = "SENT";
    public static final String PENDING = "PENDING";

    public static final String SUCCESS_CODE = "00";
    public static final String CLIENT_OK = "00";
    public static final String CLIENT_NOT_OK = "01";

    public static final String PUBLIC_CERT = "publicCert";
    public static final String TRUST_SCORE = "trustScore";
    public static final String TIMELINE = "timeline";

    //API
    public static final String API_VERSION_1 = "/v1";


}
