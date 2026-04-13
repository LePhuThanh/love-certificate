package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.CertificateSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateSessionRepository extends JpaRepository<CertificateSession, String> {
    Optional<CertificateSession> findByRequestId(String requestId);
    List<CertificateSession> findByStatus(String status);

    @Query(value = """
        SELECT cs.*
        FROM certificate_session cs
        LEFT JOIN milestone_log ml
          ON cs.session_id = ml.session_id
         AND ml.milestone_days = FLOOR(DATEDIFF(CURDATE(), cs.love_start_date))
        WHERE cs.status = 'COMPLETED'
          AND FLOOR(DATEDIFF(CURDATE(), cs.love_start_date)) IN (:milestones)
          AND ml.session_id IS NULL
    """, nativeQuery = true)
    List<CertificateSession> findEligibleSessions(@Param("milestones") List<Integer> milestones);

    //Avoid race condition
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CertificateSession s WHERE s.sessionId = :sessionId")
    Optional<CertificateSession> findBySessionIdForUpdate(@Param("sessionId") String sessionId);
}
 