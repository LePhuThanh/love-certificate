package com.phelim.system.love_certificate.config;

import com.phelim.system.love_certificate.constant.BaseConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = BaseConstants.ASYNC_NAME)
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);     // core threads
        executor.setMaxPoolSize(10);     // max threads
        executor.setQueueCapacity(50);   // task queue size

        executor.setThreadNamePrefix("LoveCert-Async-");

        executor.initialize();

        return executor;
    }
}
