package com.phelim.system.love_certificate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_job",
        indexes = {
                @Index(name = "idx_email_status", columnList = "status"),
                @Index(name = "idx_email_next_retry", columnList = "next_retry_at")
        })
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailJob {

    @Id
    @Column(name = "email_job_id")
    @NotBlank(message = "emailJobId is required")
    private String emailJobId;

    @Column(name = "to_email", nullable = false)
    @NotBlank(message = "toEmail is required")
    private String toEmail;

    @Column(name = "subject")
    @NotBlank(message = "subject is required")
    private String subject;

    @Column(name = "content", columnDefinition = "TEXT")
    @NotBlank(message = "content is required")
    private String content;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "max_retry")
    private int maxRetry;

    @Column(name = "status")
    @NotBlank(message = "status is required")
    private String status; // PENDING, PROCESSING, SENT, FAILED, DEAD

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //Case lỗi:
    //
    //Job → PROCESSING
    //→ server crash
    //→ job bị kẹt mãi
    //
    //=> Cần: Job PROCESSING quá lâu → reset về FAILED để retry lại
    @Column(name = "processing_at")
    private LocalDateTime processingAt;
}