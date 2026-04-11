package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.entity.EmailJob;
import com.phelim.system.love_certificate.repository.EmailJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailJobService {

    private final EmailJobRepository emailJobRepository;

    //Email fail lần đầu
    //=> status = PENDING
    //RetryScheduler:
    //=> xử lý PENDING
    //Nếu fail:
    //=> FAILED + retryCount++
    //Nếu quá retry:
    //=> DEAD
    public void saveFailedJob(String toEmail, String subject, String content, String error) {
        EmailJob job = EmailJob.builder()
                .emailJobId(BaseConstants.PREFIX_EMAIL_ID + UUID.randomUUID())
                .toEmail(toEmail)
                .subject(subject)
                .content(content)
                .retryCount(0)
                .maxRetry(5)
                .status(BaseConstants.PENDING)
                .nextRetryAt(LocalDateTime.now().plusMinutes(1))
                .lastError(error)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        emailJobRepository.save(job);
        log.warn("[EmailJobService][saveFailedJob] Saved failed email emailJobId={}", job.getEmailJobId());
    }

    @Transactional
    public void markSent(EmailJob job) {
        job.setStatus(BaseConstants.SENT);
        job.setProcessingAt(null); // clear
        job.setUpdatedAt(LocalDateTime.now());
        emailJobRepository.save(job);
    }

    @Transactional
    public void markRetry(EmailJob job, String error) {
        int retry = job.getRetryCount() + 1;
        job.setRetryCount(retry);
        job.setLastError(error);
        job.setUpdatedAt(LocalDateTime.now());
        job.setProcessingAt(null);

        if (retry >= job.getMaxRetry()) {
            job.setStatus(BaseConstants.DEAD);
            log.error("[EmailJobService][saveFailedJob] DEAD emailJobId={}", job.getEmailJobId());
        } else {
            job.setStatus(BaseConstants.FAILED);
            job.setNextRetryAt(LocalDateTime.now().plusMinutes(retry * 2L)); // backoff
        }
        emailJobRepository.save(job);
    }

    @Transactional
    public int claimJobs(int limit) {
        return emailJobRepository.claimJobs(limit);
    }

    @Transactional
    public int recoverStuckJobs(LocalDateTime threshold) {
        return emailJobRepository.recoverStuckJobs(threshold);
    }
}
