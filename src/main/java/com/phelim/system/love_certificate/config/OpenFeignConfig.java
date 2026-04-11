package com.phelim.system.love_certificate.config;

import com.phelim.system.love_certificate.constant.BaseConstants;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;

import java.util.concurrent.TimeUnit;

@Configuration
public class OpenFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options feignOptions(
            @Value("${love.certificate.connect-timeout-ms}") int connectTimeout,
            @Value("${love.certificate.read-timeout-ms}") int readTimeout) {

        return new Request.Options(
                connectTimeout, TimeUnit.MILLISECONDS,
                readTimeout, TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public RequestInterceptor mdcRequestInterceptor() {
        return template -> {
            String logId = MDC.get(BaseConstants.MDC_KEY);
            if (logId != null && !logId.isBlank()) {
                template.header("X-Request-ID", logId);
            }
        };
    }

}
