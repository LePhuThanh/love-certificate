package com.phelim.system.love_certificate.entity;

import com.phelim.system.love_certificate.enums.VerificationType;
import com.phelim.system.love_certificate.enums.VerifySource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "certificate_verify_log")
public class CertificateVerifyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "certId is required")
    @Column(name = "certId")
    private String certId;

    @NotNull(message = "verificationMethod is required")
    @Column(name = "verification_method")
    private VerificationType verificationMethod; // HASH | RSA | QR

    @NotBlank(message = "result is required")
    @Column(name = "result")
    private String result; // VALID | INVALID | UNVERIFIED

    @Column(name = "ip_hash")
    private String ipHash;
    @Column(name = "fingerprint")
    private String fingerprint;
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "qr_timestamp")
    private Long timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "verify_source")
    private VerifySource verifySource;
}
