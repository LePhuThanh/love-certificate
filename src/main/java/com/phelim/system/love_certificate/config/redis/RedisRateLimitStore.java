package com.phelim.system.love_certificate.config.redis;

import com.phelim.system.love_certificate.dto.ratelimit.RateLimitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimitStore {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("classpath:lua/rate_limit.lua")
    private Resource luaScript;

    public RateLimitResult check(String key, int maxAttempts, int cooldownSeconds) {

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(luaScript));
        script.setResultType(List.class);

        long now = Instant.now().getEpochSecond();

        List<?> rawResult = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                maxAttempts,
                cooldownSeconds,
                now
        );

        if (rawResult == null || rawResult.size() < 2) {
            throw new IllegalStateException("Invalid Redis Lua result");
        }

        Long status = toLong(rawResult.get(0));
        Long remaining = toLong(rawResult.get(1));

        return new RateLimitResult(status, remaining);
    }

    //Lua trả về số => Redis => Java nhận kiểu: Integer hoặc Long hoặc Double, nên dùng Number
    private Long toLong(Object value) {
        if (!(value instanceof Number)) {
            throw new IllegalStateException("Invalid number from Redis: " + value);
        }
        return ((Number) value).longValue();
    }
}