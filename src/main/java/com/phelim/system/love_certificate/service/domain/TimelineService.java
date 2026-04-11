package com.phelim.system.love_certificate.service.domain;

import com.phelim.system.love_certificate.constant.BaseConstants;
import com.phelim.system.love_certificate.dto.TimelineItem;
import com.phelim.system.love_certificate.dto.response.CertificateTimelineResponse;
import com.phelim.system.love_certificate.entity.Certificate;
import com.phelim.system.love_certificate.entity.CertificateSession;
import com.phelim.system.love_certificate.exception.BusinessException;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.model.Milestone;
import com.phelim.system.love_certificate.repository.CertificateRepository;
import com.phelim.system.love_certificate.repository.CertificateSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.phelim.system.love_certificate.constant.TimelineConstant.MILESTONES;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    private final CertificateSessionRepository sessionRepo;
    private final CertificateRepository certRepo;

    /** Cmt by Phelim (10.04.2026)
     * Cache because it's almost static and requires a lot of computing power, but it's deterministic.
     */
    @Cacheable(
            value = BaseConstants.TIMELINE,
            key = "'timeline:' + #certId"
    )
    public CertificateTimelineResponse getTimeline(String certId) {
        log.info("[CertificateServiceImpl][getTimeline] certId={}", certId);

        Certificate cert = certRepo.findById(certId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "certId=" + certId));

        CertificateSession session = sessionRepo.findById(cert.getSessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND, "certId=" + certId));

        LocalDate start = session.getLoveStartDate();
        LocalDate today = LocalDate.now();

        int totalDays = (int) ChronoUnit.DAYS.between(start, today);

        List<TimelineItem> all = buildMilestones(start, totalDays);
        enrichTimelineWithToday(all, today);

        List<TimelineItem> past = new ArrayList<>();
        List<TimelineItem> upcoming = new ArrayList<>();
        List<TimelineItem> current = new ArrayList<>();

        for (TimelineItem item : all) {
            if (item.isCurrent()) {
                current.add(item);
            } else if (item.isPassed()) {
                past.add(item);
            } else {
                upcoming.add(item);
            }
        }

        // PAST: latest (descending)
        past.sort(Comparator.comparing(TimelineItem::getDate).reversed());
        // UPCOMING: latest (ascending)
        upcoming.sort(Comparator.comparing(TimelineItem::getDate));

        // (Optional) only take the two most recent upcoming ones
        List<TimelineItem> upcomingLimited = upcoming.stream()
                .limit(2)
                .toList();

        return CertificateTimelineResponse.builder()
                .certId(certId)
                .loveStartDate(start)
                .today(today)
                .totalDays(totalDays)
                .pastMilestones(past)
                .currentMilestone(current)
                .upcomingMilestones(upcoming.stream().limit(2).toList())
                .build();
    }

    // =========================
    // COMMON METHODS
    // =========================
    private List<TimelineItem> buildMilestones(LocalDate start, int totalDays) {
        log.info("[CertificateServiceImpl][buildMilestones] start={}, totalDays={}", start, totalDays);

        List<TimelineItem> result = new ArrayList<>();
        List<Milestone> sorted = MILESTONES.stream()
                .sorted(Comparator.comparing(Milestone::getDays))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            Milestone current = sorted.get(i);

            int days = current.getDays();
            LocalDate milestoneDate = start.plusDays(days);

            boolean passed = totalDays >= days;
            // Next milestone
            int nextDays = (i < sorted.size() - 1)
                    ? sorted.get(i + 1).getDays()
                    : days;

            // Current milestone
            boolean isCurrent = totalDays >= days && totalDays < nextDays;

            // Progress %
            int progress = 0;
            if (isCurrent) {
                progress = (int) (((double) (totalDays - days) / (nextDays - days)) * 100);
            }
            result.add(TimelineItem.builder()
                    .label(current.getLabel())
                    .date(milestoneDate)
                    .days(days)
                    .passed(passed)
                    .current(isCurrent)
                    .progressPercent(progress)
                    .build());
        }
        // Sort final (past => future)
        result.sort(Comparator.comparing(TimelineItem::getDate));

        return result;
    }

    private void enrichTimelineWithToday(List<TimelineItem> milestones, LocalDate today) {
        log.info("[CertificateServiceImpl][enrichTimelineWithToday] Start");
        for (TimelineItem item : milestones) {
            LocalDate milestoneDate = item.getDate();
            // 1. Today highlight
            boolean isToday = milestoneDate.equals(today);

            // 2. Days from today
            long diff = ChronoUnit.DAYS.between(today, milestoneDate);
            // diff < 0 => past
            // diff = 0 => today
            // diff > 0 => future

            // 3. Relative text
            String relativeText = buildRelativeText(diff);
            item.setToday(isToday);
            item.setRelativeText(relativeText);
        }
    }

    private String buildRelativeText(long diff) {
        if (diff == 0) return "Today 💖";
        if (diff > 0) {
            return "In " + diff + " days 🎯";
        }
        return Math.abs(diff) + " days ago 💭";
    }

}
