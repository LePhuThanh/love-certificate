package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.CertificateVerifyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateVerifyLogRepository extends JpaRepository<CertificateVerifyLog, Long> {
    List<CertificateVerifyLog> findByCertIdOrderByVerifiedAtDesc(String certId);
}
