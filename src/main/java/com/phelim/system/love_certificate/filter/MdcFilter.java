package com.phelim.system.love_certificate.filter;

import com.phelim.system.love_certificate.constant.BaseConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/** Cmt by Phelim (10/04/2026)
 *Client
 *   ↓
 * HTTP Request
 *   ↓
 * [MdcFilter]  (HIGHEST_PRECEDENCE)
 *   - đọc X-Request-Id (header)
 *   - nếu không có → generate UUID
 *   - MDC.put(logId)
 *   - set response header
 *   ↓
 * Controller
 *   - (optional) validate body.requestId vs header
 *   ↓
 * Service / Repository
 *   - log.info(...) → tự gắn MDC
 *   ↓
 * GlobalExceptionHandler (nếu lỗi)
 *   - lấy MDC.get(logId)
 *   ↓
 * HTTP Response
 *   - luôn có X-Request-Id
 *   ↓
 * finally
 *   - MDC.clear() 🔥

 * MDC = Mapped Diagnostic Context = Thread-local map
 * 1 HTTP request = 1 thread = 1 MDC context
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader(BaseConstants.REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        try {
            // Set MDC
            MDC.put("logId", requestId);

            // Set attribute
            request.setAttribute("requestId", requestId);

            // Always return to the client
            response.setHeader(BaseConstants.REQUEST_ID_HEADER, requestId);

            log.debug("[MdcFilter][doFilterInternal] requestId={}", requestId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}