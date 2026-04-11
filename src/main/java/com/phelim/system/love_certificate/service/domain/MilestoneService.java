package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.constant.CertSessionStatus;
import com.phelim.system.love_certificate.entity.CertificateSession;
import com.phelim.system.love_certificate.entity.MilestoneLog;
import com.phelim.system.love_certificate.model.Milestone;
import com.phelim.system.love_certificate.constant.TimelineConstant;
import com.phelim.system.love_certificate.repository.CertificateSessionRepository;
import com.phelim.system.love_certificate.repository.MilestoneLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MilestoneService {

    private final CertificateSessionRepository sessionRepo;
    private final MilestoneLogRepository milestoneLogRepo;
    private final EmailService emailService;

    @Value("${sms.environment}")
    private String environment;

    public void processRelationshipMilestones() {
        log.info("[MilestoneService][processRelationshipMilestones] Start checking.");

        // 1. Load completedSessions
        List<CertificateSession> completedSessions = sessionRepo.findByStatus(CertSessionStatus.COMPLETED);

        if (completedSessions.isEmpty()) {
            log.info("[MilestoneService][processRelationshipMilestones] No completed sessions");
            return;
        }
        LocalDate currentDate = LocalDate.now();

        // 2. Pre-calc milestone days set (O(1) lookup) => Set.contains() = O(1), List.contains() = O(n)
        //[0, 30, 100, 365, 730, 1825, 3650] => Set
        Set<Integer> milestoneDaysSet = TimelineConstant.MILESTONES.stream()
                .map(Milestone::getDays)
                .collect(Collectors.toSet());


        Map<String, Integer> candidateMap = new HashMap<>(); //key: sessionId, value: milestoneDays
        for (CertificateSession session : completedSessions) {
            long daysSinceRelationshipStart = ChronoUnit.DAYS.between(session.getLoveStartDate(), currentDate);

            // 3. Collect only sessions that can hit milestone currentDate
            if (milestoneDaysSet.contains((int) daysSinceRelationshipStart)) {
                candidateMap.put(session.getSessionId(), (int) daysSinceRelationshipStart);
            }
        }

        if (candidateMap.isEmpty()) {
            log.info("[MilestoneService][processRelationshipMilestones] No milestone hit today");
            return;
        }

        // 4. Batch query existing logs (FIX N+1) (1000 query => 1 query)
        List<String> candidateSessionIds = new ArrayList<>(candidateMap.keySet());
        List<Object[]> existingLogs = milestoneLogRepo.findExistingLogs(candidateSessionIds);

        //CS_123_365 => to check existingSet.contains(key)
        Set<String> existingSet = existingLogs.stream()
                .map(r -> r[0] + "_" + r[1])
                .collect(Collectors.toSet());

        // 5. Process only candidate sessions
        for (CertificateSession session : completedSessions) {

            Integer milestoneDays = candidateMap.get(session.getSessionId());
            if (milestoneDays == null) continue;

            String key = session.getSessionId() + "_" + milestoneDays;
            if (existingSet.contains(key)) {
                log.info("[MilestoneService][processRelationshipMilestones] Skip duplicate sessionId={}, days={}",
                        session.getSessionId(), milestoneDays);
                continue;
            }

            Milestone milestone = TimelineConstant.MILESTONES.stream()
                    .filter(m -> m.getDays() == milestoneDays)
                    .findFirst()
                    .orElse(null);

            if (milestone == null) continue;
            log.info("[Milestone HIT][processRelationshipMilestones] sessionId={} days={} label={}",
                    session.getSessionId(), milestoneDays, milestone.getLabel());

            // Send email
            if (session.getEmail() != null && !session.getEmail().isBlank()) {
                if (BaseConstants.TEST_ENVIRONMENT.equalsIgnoreCase(environment)) {
                    log.info("[MilestoneService][processRelationshipMilestones] TEST env - Skip sending email");
                } else {
                    emailService.sendMilestoneEmail(session.getEmail(), session.getMaleName(), session.getFemaleName(),
                            milestoneDays, milestone.getLabel());
                }
            } else {
                log.warn("[MilestoneService][processRelationshipMilestones] Missing email sessionId={}", session.getSessionId());
            }
            // Save log
            persistMilestoneLog(session.getSessionId(), milestone);
        }
        log.info("[MilestoneService][processRelationshipMilestones] Done");
    }


    //Optimized (VIP PRO)
    public void processRelationshipMilestones3() {
        log.info("[MilestoneService][ULTIMATE] Start checking.");

        // 1. Extract milestone days
        List<Integer> milestoneDays = TimelineConstant.MILESTONES.stream()
                .map(Milestone::getDays)
                .toList();

        // 2. Query directly eligible sessions (only needed data)
        List<CertificateSession> sessions = sessionRepo.findEligibleSessions(milestoneDays);
        if (sessions.isEmpty()) {
            log.info("[MilestoneService][ULTIMATE] No eligible sessions");
            return;
        }

        LocalDate today = LocalDate.now();
        for (CertificateSession session : sessions) {

            int daysSinceRelationshipStart = (int) ChronoUnit.DAYS.between(session.getLoveStartDate(), today);
            // 3. Find milestone config
            Milestone milestone = TimelineConstant.MILESTONES.stream()
                    .filter(m -> m.getDays() == daysSinceRelationshipStart)
                    .findFirst()
                    .orElse(null);

            if (milestone == null) {
                log.warn("[MilestoneService][ULTIMATE] milestone config missing days={}", daysSinceRelationshipStart);
                continue;
            }
            log.info("[Milestone HIT][ULTIMATE] sessionId={} days={} label={}", session.getSessionId(), daysSinceRelationshipStart, milestone.getLabel());

            // 4. Send email
            if (session.getEmail() != null && !session.getEmail().isBlank()) {
                if (BaseConstants.TEST_ENVIRONMENT.equalsIgnoreCase(environment)) {
                    log.info("[MilestoneService][ULTIMATE] TEST env - Skip email");
                } else {
                    emailService.sendMilestoneEmail(session.getEmail(), session.getMaleName(),
                            session.getFemaleName(), daysSinceRelationshipStart, milestone.getLabel());
                }
            } else {
                log.warn("[MilestoneService][ULTIMATE] Missing email sessionId={}", session.getSessionId());
            }
            // 5. Save log (idempotent)
            persistMilestoneLog(session.getSessionId(), milestone);
        }
        log.info("[MilestoneService][ULTIMATE] Done");
    }

    //N+1 (CHICKEN)
    public void processRelationshipMilestones2() {
        log.info("[MilestoneService][processRelationshipMilestones2] Start checking.");

        // Only Certificate Session COMPLETED
        List<CertificateSession> completedSessions = sessionRepo.findByStatus(CertSessionStatus.COMPLETED);

        LocalDate currentDate = LocalDate.now();
        for (CertificateSession session : completedSessions) {
            long daysSinceRelationshipStart = ChronoUnit.DAYS.between(
                    session.getLoveStartDate(),
                    currentDate
            );
            // 9:00 AM
            // => Scheduler run => check milestone
            // CASE 1 (sent):
            // => log  => save MilestoneLog
            // CASE 2 (not sent): => skip
            TimelineConstant.MILESTONES.forEach(milestone -> {
                int milestoneDays = milestone.getDays();
                if (milestoneDays == daysSinceRelationshipStart) {
                    // 1. Check already sent
                    boolean isMilestoneAlreadyLogged = milestoneLogRepo.existsBySessionIdAndMilestoneDays(session.getSessionId(), milestone.getDays());

                    if (isMilestoneAlreadyLogged) {
                        log.info("[MilestoneService][processRelationshipMilestones2] Skip duplicate sessionId={}, days={}",
                                session.getSessionId(), milestone.getDays());
                        return;
                    }

                    // 2. Trigger (TEST environment only to log, LIVE environment will be sent via email)
                    if (session.getEmail() != null && !session.getEmail().isBlank()) {
                        if (BaseConstants.TEST_ENVIRONMENT.equalsIgnoreCase(environment)){
                            log.info("[MilestoneService][processRelationshipMilestones2] TEST environment - Skip sending mail");
                            log.info("[MilestoneService][processRelationshipMilestones2] Milestone reached: sessionId={} daysSinceStart={} label={}",
                                    session.getSessionId(), daysSinceRelationshipStart, milestone.getLabel());
                        } else {
                            // Send email
                            emailService.sendMilestoneEmail(session.getEmail(), session.getMaleName(), session.getFemaleName(),
                                    milestoneDays, milestone.getLabel());
                        }
                    } else {
                        log.warn("[MilestoneService][processRelationshipMilestones2] Missing email sessionId={}", session.getSessionId());
                    }
                    // 3. Save log (marked as submitted)
                    persistMilestoneLog(session.getSessionId(), milestone );
                }
            });
        }
        log.info("[MilestoneService][processRelationshipMilestones2] Done");
    }

    private void persistMilestoneLog(String sessionId, Milestone milestone) {
        MilestoneLog logEntry = MilestoneLog.builder()
                .logId(BaseConstants.PREFIX_MILESTONE_LOG_ID + UUID.randomUUID())
                .sessionId(sessionId)
                .milestoneDays(milestone.getDays())
                .label(milestone.getLabel())
                .triggeredAt(LocalDateTime.now())
                .build();
        try {
            milestoneLogRepo.save(logEntry);
        } catch (Exception ex) {
            // double protection if race condition
            log.warn("[MilestoneService][persistMilestoneLog] Duplicate detected sessionId={}, days={}",
                    sessionId, milestone.getDays());
        }
    }
}
