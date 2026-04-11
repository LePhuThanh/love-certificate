package com.phelim.system.love_certificate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificate_session")
public class CertificateSession {

    @Id
    @NotBlank(message = "sessionId is required")
    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "request_id", unique = true)
    @NotBlank(message = "requestId is required")
    private String requestId;

    @Column(name = "male_name")
    @NotBlank(message = "maleName is required")
    private String maleName;
    @Column(name = "female_name")
    @NotBlank(message = "femaleName is required")
    private String femaleName;

    @Column(name = "male_age")
    private Integer maleAge;
    @Column(name = "female_age")
    private Integer femaleAge;

    @Column(name = "love_start_date")
    @NotNull(message = "loveStartDate is required")
    private LocalDate loveStartDate;

    @Column(name = "status")
    private String status; // DRAFT, OTP_PENDING, VERIFIED, PROCESSING, COMPLETED, FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Otp
    @Column(name = "otp_code")
    private String otpCode;
    @Column(name = "otp_expire_at")
    private LocalDateTime otpExpireAt;
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    //    @Pattern(
//            regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
//            message = "Invalid email format"
//    )
    @NotBlank(message = "email is required")
    @Email
    @Column(name = "email")
    private String email;
}
