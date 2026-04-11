package com.phelim.system.love_certificate.client;

import com.phelim.system.love_certificate.config.OpenFeignConfig;
import com.phelim.system.love_certificate.dto.feignclient.LoveCertificateResponse;
import com.phelim.system.love_certificate.dto.feignclient.SmsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sms-client", url = "${sms.base-url}", configuration = OpenFeignConfig.class)
public interface SmsClient {
    @PostMapping(value = "${sms.url.send-otp}", consumes = MediaType.APPLICATION_JSON_VALUE)
    LoveCertificateResponse<Boolean> sendOtp(@RequestBody SmsRequest request);
}
