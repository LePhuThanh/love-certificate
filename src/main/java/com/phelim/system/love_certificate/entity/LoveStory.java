package com.phelim.system.love_certificate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "love_story")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoveStory {

    @Id
    @Column(name = "story_id")
    @NotBlank(message = "storyId is required")
    private String storyId;

    @NotBlank(message = "sessionId is required")
    @Column(name = "session_id")
    private String sessionId;

    @NotBlank(message = "content is required")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "version is required")
    @Column(name = "version")
    private Integer version;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
