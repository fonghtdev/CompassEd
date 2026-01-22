package com.compassed.compassed_api.domain.entity;

import com.compassed.compassed_api.domain.enums.Level;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="placement_results")
@Getter @Setter
public class PlacementResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false)
    @JoinColumn(name="subject_id")
    private Subject subject;

    private Double scorePercent;

    @Enumerated(EnumType.STRING)
    private Level level;

    // JSON phân tích kỹ năng từ AI
    @Column(columnDefinition = "json")
    private String skillAnalysisJson;

    private LocalDateTime createdAt;
}
