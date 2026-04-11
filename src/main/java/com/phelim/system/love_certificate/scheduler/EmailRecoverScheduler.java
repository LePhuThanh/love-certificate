package com.phelim.system.love_certificate.scheduler;

import com.phelim.system.love_certificate.config.scheduler.EmailJobProperties;
import com.phelim.system.love_certificate.constant.SchedulerPropertiesKeys;
import com.phelim.system.love_certificate.service.domain.EmailJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Self-heal system
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = SchedulerPropertiesKeys.EMAIL_RECOVERY_ENABLED,
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class EmailRecoverScheduler {

    private final EmailJobService emailJobService;
    private final EmailJobProperties emailJobProps;

    @Scheduled(fixedRate = 120000) // 2 minute
    public void recoverStuckJobs() {
        log.info("[EmailRecoverScheduler][recoverStuckJobs] Start to run the ----------RECOVER-STUCK-JOBS-SCHEDULER---------- job");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(emailJobProps.getTimeoutMinutes());
        int updated = emailJobService.recoverStuckJobs(threshold);

        if (updated > 0) {
            log.warn("[EmailRecoverScheduler][recoverStuckJobs] Recovered {} stuck jobs", updated);
        }
        log.info("[EmailRecoverScheduler][recoverStuckJobs] Done the ----------RECOVER-STUCK-JOBS-SCHEDULER---------- job");
    }
}
