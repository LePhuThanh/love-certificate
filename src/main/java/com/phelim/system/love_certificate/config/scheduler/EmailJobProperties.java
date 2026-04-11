package com.phelim.system.love_certificate.config.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email.job")
@Getter
@Setter
public class EmailJobProperties {
    private int claimLimit;
    private int timeoutMinutes;
}
