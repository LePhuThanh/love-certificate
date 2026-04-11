package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.EmailJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailJobRepository extends JpaRepository<EmailJob, String> {

    //The email job has been picked => marking for processing_at.
    @Modifying
    @Query(value = """
        UPDATE email_job
        SET status = 'PROCESSING',
            processing_at = NOW()
        WHERE email_job_id IN (
            SELECT email_job_id FROM email_job
            WHERE status IN ('PENDING','FAILED')
              AND next_retry_at <= NOW()
            LIMIT :limit
        )
    """, nativeQuery = true)
    int claimJobs(@Param("limit") int limit);

    //EmailJob takes longer than 5 minutes(conf in application.properties) to process => considered stuck => reset
    @Modifying
    @Query(value = """
        UPDATE email_job
        SET status = 'FAILED'
        WHERE status = 'PROCESSING'
          AND processing_at < :threshold
    """, nativeQuery = true)
    int recoverStuckJobs(@Param("threshold") LocalDateTime threshold);

    @Query("""
        SELECT e FROM EmailJob e
        WHERE e.status = 'PROCESSING'
    """)
    List<EmailJob> findProcessingJobs();
}
