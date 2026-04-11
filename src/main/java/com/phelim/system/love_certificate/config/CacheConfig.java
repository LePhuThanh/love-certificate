package com.phelim.system.love_certificate.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        List<CaffeineCache> caches = new ArrayList<>();

        // PUBLIC CERT (heavy)
        caches.add(new CaffeineCache("publicCert",
                Caffeine.newBuilder()
                        .maximumSize(5000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .build()));

        // TRUST SCORE (dynamic)
        caches.add(new CaffeineCache("trustScore",
                Caffeine.newBuilder()
                        .maximumSize(5000)
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .build()));

        // TIMELINE (almost static)
        caches.add(new CaffeineCache("timeline",
                Caffeine.newBuilder()
                        .maximumSize(5000)
                        .expireAfterWrite(1, TimeUnit.DAYS)
                        .build()));

        manager.setCaches(caches);
        return manager;
    }
}
