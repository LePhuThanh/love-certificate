package com.phelim.system.love_certificate.scheduler;

import com.phelim.system.love_certificate.config.scheduler.EmailJobProperties;
import com.phelim.system.love_certificate.constant.SchedulerPropertiesKeys;
import com.phelim.system.love_certificate.entity.EmailJob;
import com.phelim.system.love_certificate.repository.EmailJobRepository;
import com.phelim.system.love_certificate.service.domain.EmailJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// Process queue
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = SchedulerPropertiesKeys.EMAIL_RETRY_ENABLED,
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class EmailRetryScheduler {

    private final JavaMailSender mailSender;
    private final EmailJobRepository emailJobRepository;
    private final EmailJobService emailJobService;
    private final EmailJobProperties emailJobProps;

    @Scheduled(fixedRate = 30000) // 30s
    public void retryEmails() {
        log.info("[EmailRetryScheduler][retryEmails] [DB-LOCK] Start to run the ----------EMAIL-RETRY-SCHEDULER---------- job");

        // 1. Claim job
        int claimed = emailJobService.claimJobs(emailJobProps.getClaimLimit());
        if (claimed == 0) {
            log.info("[EmailRetryScheduler][retryEmails] No jobs to process");
            return;
        }

        // 2. Load claimed jobs
        List<EmailJob> jobs = emailJobRepository.findProcessingJobs();
        for (EmailJob job : jobs) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(job.getToEmail());
                message.setSubject(job.getSubject());
                message.setText(job.getContent());

                mailSender.send(message);
                emailJobService.markSent(job);
            } catch (Exception ex) {
                emailJobService.markRetry(job, ex.getMessage());
            }
        }
        log.info("[EmailRetryScheduler][retryEmails] [DB-LOCK] Done the ----------EMAIL-RETRY-SCHEDULER---------- job");
    }
}
