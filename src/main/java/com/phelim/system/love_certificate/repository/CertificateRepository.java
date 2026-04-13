package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
    Optional<Certificate> findBySessionId(String sessionId);
}