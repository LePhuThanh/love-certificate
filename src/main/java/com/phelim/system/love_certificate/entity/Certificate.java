package com.phelim.system.love_certificate.entity;

import com.phelim.system.love_certificate.enums.CertificateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificate")
public class Certificate {

    @Id
    @NotBlank(message = "certId is required")
    @Column(name = "cert_id")
    private String certId;

    @NotBlank(message = "sessionId is required")
    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "duration_days")
    private int durationDays;
    @Column(name = "type")
    @NotNull(message = "type is required")
    private CertificateType type; // A, B, C

    @Column(name = "file_url")
    @NotBlank(message = "fileUrl is required")
    private String fileUrl;
    @Column(name = "file_hash")
    private String fileHash; // SHA-256

    @Column(name = "signature", columnDefinition = "TEXT") // using TEXT for:signature, token, payload JSON ,certificate
//    @Column(length = 1000)
    private String signature;

    @Column(name = "issue_at")
    private LocalDateTime issuedAt;

    @Builder.Default
    @Column(name = "revoked")
    private Boolean revoked = false;
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    @Column(name = "revoked_reason")
    private String revokedReason;
}