package com.phelim.system.love_certificate.repository;

import com.phelim.system.love_certificate.entity.MilestoneLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MilestoneLogRepository extends JpaRepository<MilestoneLog, String> {

    @Query("""
        SELECT ml.sessionId, ml.milestoneDays
        FROM MilestoneLog ml
        WHERE ml.sessionId IN :sessionIds
    """)
    List<Object[]> findExistingLogs(@Param("sessionIds") List<String> sessionIds);

    boolean existsBySessionIdAndMilestoneDays(String sessionId, int milestoneDays);
}
