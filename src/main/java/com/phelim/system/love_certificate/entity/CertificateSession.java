package com.phelim.system.love_certificate.entity;

import com.phelim.system.love_certificate.enums.CertSessionStatus;
import com.phelim.system.love_certificate.enums.Region;
import com.phelim.system.love_certificate.validation.annotation.PhoneNumber;
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
    private String status; // DRAFT, OTP_PENDING, VERIFIED, PROCESSING, COMPLETED, OTP_FAILED, FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Otp
    @Column(name = "otp")
    @Pattern(regexp = "^\\d{6}$", message = "Otp must be 6 digits")
    private String otp;
    //    @Column(name = "otp_hash")
//    private String otpHash;
//    @Column(name = "otp_salt")
//    private String otpSalt;
    @Column(name = "otp_expire_at")
    private LocalDateTime otpExpireAt;
    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    //    @Pattern(
//            regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
//            message = "Invalid email format"
//    )
    @NotBlank(message = "email is required")
    @Email
    @Column(name = "email")
    private String email;

    @NotBlank(message = "phoneNumber is required")
    @PhoneNumber(allowInternational = true)
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "region")
    @NotNull(message = "region is required")
    private Region region;



    public boolean isOtpExpired() {
        return otpExpireAt != null && LocalDateTime.now().isAfter(otpExpireAt);
    }

    public boolean isMaxRetryExceeded(int maxRetry) {
        return retryCount >= maxRetry;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean isCompleted() {
        return CertSessionStatus.COMPLETED.equals(status);
    }
}
