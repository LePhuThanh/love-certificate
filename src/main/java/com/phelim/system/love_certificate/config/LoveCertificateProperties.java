package com.phelim.system.love_certificate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "love.certificate")
@Getter
@Setter
public class LoveCertificateProperties {
    /**
     * OTP expiration time in seconds (default: 120 seconds = 2 minutes)
     */
    private int otpExpireSeconds = 120;

    /**
     * Maximum number of OTP retry attempts (default: 5)
     */
    private int maxOtpRetry = 5;

    /**
     * OTP length in digits (default: 6)
     */
    private int otpLength = 6;

    private int otpSaltLength = 16;
    private int iterations = 10000;
    private int keyLength = 256;

}
