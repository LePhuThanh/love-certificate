package com.phelim.system.love_certificate.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineItem {
    private String label;
    private LocalDate date;
    private int days;
    private boolean passed;

    private boolean current;
    private int progressPercent; // 0 => 100

    private boolean isToday;
    private String relativeText; // "3 days ago", "in 5 days"
}
