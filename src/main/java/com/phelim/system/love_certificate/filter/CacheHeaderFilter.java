package com.phelim.system.love_certificate.filter;

import com.phelim.system.love_certificate.constant.BaseConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE) // run after MdcFilter
@Slf4j
public class CacheHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            // Default HIT if AOP is not set
            String cacheStatus = (String) request.getAttribute(BaseConstants.CACHE_ATTR);
            if (cacheStatus == null) {
                cacheStatus = BaseConstants.HIT_CACHE;
            }
            String cacheName = (String) request.getAttribute(BaseConstants.CACHE_NAME_ATTR);
            String cacheKey = (String) request.getAttribute(BaseConstants.CACHE_KEY_ATTR);

            // fallback if HIT (AOP don't run)
            if (cacheName == null) {
                cacheName = resolveCacheNameFromPath(request.getRequestURI());
            }

            if (cacheKey != null) {
                response.setHeader("X-Cache-Key", cacheKey);
            }

            // Set header
            response.setHeader("X-Cache", cacheStatus);
            response.setHeader("X-Cache-Name", cacheName);

            log.debug("[CacheHeaderFilter][doFilterInternal] X-Cache={}, name={}, key={}",
                    cacheStatus, cacheName, cacheKey);
        }
    }

    private String resolveCacheNameFromPath(String path) {
        if (path.contains("/public/cert/")) {
            return BaseConstants.PUBLIC_CERT;
        }
        if (path.contains("/trust-score")) {
            return BaseConstants.TRUST_SCORE;
        }
        if (path.contains("/timeline")) {
            return BaseConstants.TIMELINE;
        }
        return BaseConstants.UNKNOWN_CAPITALIZED;
    }
}
