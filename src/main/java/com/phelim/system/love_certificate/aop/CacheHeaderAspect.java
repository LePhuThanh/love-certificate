package com.phelim.system.love_certificate.aop;

import com.phelim.system.love_certificate.constant.BaseConstants;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;


/** Cmt by Pheim (10/04/2026)
 * If the method with @Cacheable is executed => MISS cache.
 * If the method is not executed => HIT cache.

 * Request => Spring Cache Proxy
 * IF HIT:
 * => always return
 * => AOP does not run
 * => no X_CACHE => => HIT
 * IF MISS:
 * => call method
 * => AOP runs
 * => set X_CACHE = MISS

 * CACHE TESTING PRINCIPLES: We need to verify 3 things:
 * 1. MISS Cache
 * => The first time, the logic must run correctly.
 * 2. HIT Cache
 * => The next time, the logic must NOT run.
 * 3. EVICT Cache
 * => After an update, the cache is cleared, so calling it again must result in a MISS.
 */
@Aspect
@Component
@Slf4j
public class CacheHeaderAspect {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    //Intercept all methods annotated with @Cacheable
    @Around("@annotation(cacheable)")
    public Object around(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {

        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            // Detect MISS
            attrs.setAttribute(BaseConstants.CACHE_ATTR, BaseConstants.MISS_CACHE, RequestAttributes.SCOPE_REQUEST);

            // Get cache name (auto from annotation)
            String cacheName = resolveCacheName(cacheable);
            attrs.setAttribute(BaseConstants.CACHE_NAME_ATTR, cacheName, RequestAttributes.SCOPE_REQUEST);

            // Get Cache key (evaluate SpEL)
            String cacheKey = resolveCacheKey(joinPoint, cacheable);
            if (cacheKey != null) {
                attrs.setAttribute(BaseConstants.CACHE_KEY_ATTR, cacheKey, RequestAttributes.SCOPE_REQUEST);
            }

            log.debug("[CacheHeaderAspect][around] MISS cacheName={}", cacheName);
        }
        return joinPoint.proceed();
    }


    // Resolve cache name safely
    private String resolveCacheName(Cacheable cacheable) {
        if (cacheable.value().length > 0) {
            return cacheable.value()[0];
        }
        if (cacheable.cacheNames().length > 0) {
            return cacheable.cacheNames()[0];
        }
        return BaseConstants.UNKNOWN_CAPITALIZED;
    }

    private String resolveCacheKey(ProceedingJoinPoint joinPoint, Cacheable cacheable) {
        try {
            String keyExpression = cacheable.key();

            if (keyExpression == null || keyExpression.isBlank()) {
                return BaseConstants.KEY_DEFAULT_NAME_CAPITALIZED; // if no key is set
            }

            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] args = joinPoint.getArgs();

            MethodBasedEvaluationContext context =
                    new MethodBasedEvaluationContext(joinPoint.getTarget(), method, args, nameDiscoverer);

            Expression expression = parser.parseExpression(keyExpression);

            Object value = expression.getValue(context);

            return value != null ? value.toString() : null;

        } catch (Exception ex) {
            log.warn("[CacheHeaderAspect][resolveCacheKey] Cannot evaluate cache key", ex);
            return BaseConstants.UNKNOWN_CAPITALIZED;
        }
    }
}