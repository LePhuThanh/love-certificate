package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.client.SmsClient;
import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.feignclient.LoveCertificateResponse;
import com.phelim.system.love_certificate.dto.feignclient.SmsRequest;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final SmsClient smsClient;

    // =========================
    // SMS CLIENT
    // =========================
    public void sendOtp(String phoneNumber, String partnerName, String message){
        log.info("[ClientService][sendOtp] Start calling SMS CLIENT. phoneNumber={}, partnerName={}", phoneNumber, partnerName);

        SmsRequest request = SmsRequest.builder()
                .phoneNumber(phoneNumber)
                .content(message)
                .build();
        try {
            LoveCertificateResponse<Boolean> response = smsClient.sendOtp(request);

            if (response == null) {
                log.warn("[ClientService][sendOtp] Null response from SMS CLIENT. phoneNumber={}, phoneNumber={}", phoneNumber, partnerName);
                throw new BusinessException(ErrorCode.FEIGN_CLIENT_INVALID_RESPONSE,
                        String.format("phoneNumber = %s, partnerName = %s", phoneNumber, partnerName));
            }

            final String code = response.getCode();

            if (BaseConstants.CLIENT_NOT_OK.equals(code)) {
                log.warn("[ClientService][sendOtp] Not supported. phoneNumber={}, partnerName={}", phoneNumber, partnerName);
                throw new BusinessException(ErrorCode.FEIGN_CLIENT_DETAIL_MESSAGE_ERROR_FOR_NOT_OK, "Error from SMS CLIENT",
                        String.format("phoneNumber = %s, partnerName = %s", phoneNumber, partnerName));
            }

            if (!BaseConstants.CLIENT_OK.equals(code)) {
                log.error("[ClientService][sendOtp] SMS CLIENT business error. phoneNumber={}, partnerName={}, code={}, message={}",
                        phoneNumber, partnerName, code, response.getMessage());
                throw new BusinessException(ErrorCode.FEIGN_CLIENT_BUSINESS_ERROR,
                        String.format("phoneNumber = %s, partnerName = %s, code = %s, message = %s", phoneNumber, partnerName, response.getCode(), response.getMessage()));
            }

            final Boolean sentSuccess = response.getData();
            if (!Boolean.TRUE.equals(sentSuccess)) {
                log.error("[ClientService][sendOtp] OTP sending failed. phoneNumber={}, partnerName={}, code={}, message={}",
                        phoneNumber, partnerName, response.getCode(), response.getMessage());
                throw new BusinessException(ErrorCode.OTP_SEND_FAILED,
                        String.format("phoneNumber = %s, partnerName = %s, code = %s, message = %s", phoneNumber, partnerName, response.getCode(), response.getMessage()));
            }

            //Success
            log.info("[ClientService][sendOtp] Calling SMS CLIENT success. phoneNumber={}, partnerName={}", phoneNumber, partnerName);

        } catch (BusinessException ex) {
            throw ex;
        } catch (feign.RetryableException ex) {
            log.error("[ClientService][sendOtp] Timeout calling SMS CLIENT.", ex);
            throw new BusinessException(ErrorCode.TIMEOUT_ERROR, "Exception = " + ex);
        } catch (feign.FeignException ex) {
            log.error("[ClientService][sendOtp] SMS CLIENT error. status={}, message={}",
                    ex.status(), ex.getMessage(), ex);
            throw mapFeignException(ex);
        } catch (Exception ex) {
            log.error("[ClientService][sendOtp] Unexpected error", ex);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Exception = " + ex);
        }
    }

    public BusinessException mapFeignException(feign.FeignException ex) {
        return switch (ex.status()) {
            case 400 -> new BusinessException(ErrorCode.FEIGN_CLIENT_INVALID_RESPONSE, "Exception = " + ex);
            case 404 -> new BusinessException(ErrorCode.FEIGN_CLIENT_NOT_FOUND, "Exception = " + ex);
            case 408, 504 -> new BusinessException(ErrorCode.TIMEOUT_ERROR, "Exception = " + ex);
            case 500, 502, 503 -> new BusinessException(ErrorCode.DOWNSTREAM_SERVICE_UNAVAILABLE, "Exception=" + ex);
            default -> new BusinessException(ErrorCode.FEIGN_CLIENT_UNAVAILABLE, "Exception = " + ex);
        };
    }
}
