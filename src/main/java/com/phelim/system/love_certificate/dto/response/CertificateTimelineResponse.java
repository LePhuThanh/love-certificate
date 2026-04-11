package com.phelim.system.love_certificate.dto.response;

import com.phelim.system.love_certificate.dto.TimelineItem;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateTimelineResponse {

    private String certId;
    private LocalDate loveStartDate;
    private LocalDate today;
    private int totalDays;

    private List<TimelineItem> pastMilestones;
    private List<TimelineItem> currentMilestone;
    private List<TimelineItem> upcomingMilestones;
}
