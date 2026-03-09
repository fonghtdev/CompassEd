package com.compassed.compassed_api.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "user_ai_roadmap_states",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"})
)
@Getter
@Setter
public class UserAiRoadmapState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "roadmap_guide_json", columnDefinition = "LONGTEXT")
    private String roadmapGuideJson;

    @Column(name = "refresh_count", nullable = false)
    private Integer refreshCount = 0;

    @Column(name = "initialized_at")
    private LocalDateTime initializedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

