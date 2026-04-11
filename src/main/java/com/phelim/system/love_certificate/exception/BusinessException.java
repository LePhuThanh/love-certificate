package com.phelim.system.love_certificate.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public BusinessException(ErrorCode errorCode, String details) {
        super(details == null
                ? errorCode.getMessage()
                : errorCode.getMessage() + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

    public BusinessException(ErrorCode errorCode, String customMessage, String details) {
        super(details == null
                ? customMessage
                : customMessage + ": " + details);
        this.errorCode = errorCode;
        this.details = details;
    }

}