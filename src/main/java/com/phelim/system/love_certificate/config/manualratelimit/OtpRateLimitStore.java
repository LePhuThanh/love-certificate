package com.phelim.system.love_certificate.config.manualratelimit;

import com.phelim.system.love_certificate.dto.ratelimit.RateLimitInfo;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpRateLimitStore {
    //ConcurrentHashMap in heap memory (RAM)
    private final ConcurrentHashMap<String, RateLimitInfo> store = new ConcurrentHashMap<>();

    public RateLimitInfo get(String key) {
        return store.get(key);
    }

    public void put(String key, RateLimitInfo value) {
        store.put(key, value);
    }

    public void remove(String key) {
        store.remove(key);
    }
}
