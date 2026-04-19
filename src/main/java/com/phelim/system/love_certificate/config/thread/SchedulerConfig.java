package com.phelim.system.love_certificate.config.thread;

import com.phelim.system.love_certificate.constant.BaseConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {
    @Bean(name = BaseConstants.EXECUTOR_SCHEDULER)
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // core threads
        executor.setMaxPoolSize(10); // max threads
        executor.initialize();
        return executor;
    }
}