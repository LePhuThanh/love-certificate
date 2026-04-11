package com.phelim.system.love_certificate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

//Each milestone should only be triggered once per session per day. (Mỗi milestone chỉ nên trigger 1 lần / 1 session / 1 ngày-mốc)
@Entity
@Table(
        name = "milestone_log",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_session_milestone", columnNames = {"session_id", "milestone_days"})
        }
)
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilestoneLog {

    @Id
    @Column(name = "log_id")
    @NotBlank(message = "logId is required")
    private String logId;

    @NotBlank(message = "sessionId is required")
    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "milestone_days", nullable = false)
    private int milestoneDays;

    @Column(name = "label")
    private String label;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;
}
