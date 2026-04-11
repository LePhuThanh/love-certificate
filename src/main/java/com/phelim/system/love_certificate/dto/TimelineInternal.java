package com.phelim.system.love_certificate.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineInternal {

    private String label;
    private LocalDate date;
    private int days;

    private boolean passed;
    private boolean current;
    private int progressPercent;

    // raw data
    private long daysFromToday; // âm: quá khứ, dương: tương lai
}
