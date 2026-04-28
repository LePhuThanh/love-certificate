package com.phelim.system.love_certificate.controller;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.feignclient.LoveCertificateResponse;
import com.phelim.system.love_certificate.dto.monitoring.CircuitBreakerInfo;
import com.phelim.system.love_certificate.dto.monitoring.RetryInfo;
import com.phelim.system.love_certificate.service.monitoring.MonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Slf4j
public class MonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/v1/circuit-breakers")
    public LoveCertificateResponse<List<CircuitBreakerInfo>> getCircuitBreakers(HttpServletRequest request) {

        String requestId = resolveRequestId(request);

        return LoveCertificateResponse.success(requestId, monitoringService.getCircuitBreakers());
    }

    @GetMapping("/v1/retries")
    public LoveCertificateResponse<List<RetryInfo>> getRetries(HttpServletRequest request) {

        String requestId = resolveRequestId(request);

        return LoveCertificateResponse.success(requestId, monitoringService.getRetries());
    }

    @GetMapping("/v1/circuit-breakers/{name}")
    public LoveCertificateResponse<CircuitBreakerInfo> getCircuitBreaker(@PathVariable String name, HttpServletRequest request) {

        String requestId = resolveRequestId(request);

        log.info("[MonitoringController][getCircuitBreaker] requestId={}, circuitName={}",
                requestId, name);

        CircuitBreakerInfo data = monitoringService.getCircuitBreaker(name);

        return LoveCertificateResponse.success(requestId, data);
    }

    @PostMapping("/v1/circuit-breakers/{name}/reset")
    public LoveCertificateResponse<String> resetCircuit(@PathVariable String name, HttpServletRequest request) {

        String requestId = resolveRequestId(request);

        log.warn("[MonitoringController][resetCircuit] requestId={}, circuitName={}",
                requestId, name);

        monitoringService.resetCircuit(name);

        return LoveCertificateResponse.success(requestId, "RESET SUCCESS");
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object requestId = request.getAttribute(BaseConstants.REQUEST_ID);

        if (requestId != null) {
            return requestId.toString();
        }
        // fallback for monitoring / internal API
        return "MON-" + System.currentTimeMillis();
    }
}
