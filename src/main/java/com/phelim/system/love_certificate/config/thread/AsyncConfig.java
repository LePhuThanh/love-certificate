package com.phelim.system.love_certificate.config.thread;

import com.phelim.system.love_certificate.constant.BaseConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = BaseConstants.EXECUTOR_ASYNC_GENERATE_CER)
    public Executor generateCerExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5);     // core threads
        executor.setMaxPoolSize(10);     // max threads
        executor.setQueueCapacity(50);   // task queue size

        executor.setThreadNamePrefix("LoveCert-Async-");

        executor.initialize();

        return executor;
    }

    @Bean(name = BaseConstants.EXECUTOR_ASYNC_SMS)
    public Executor smsExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("Sms-Async-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();

        return executor;
    }
}