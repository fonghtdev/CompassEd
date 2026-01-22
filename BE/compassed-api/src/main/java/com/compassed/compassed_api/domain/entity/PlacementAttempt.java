package com.compassed.compassed_api.domain.entity;

import com.compassed.compassed_api.domain.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="placement_attempts")
@Getter @Setter
public class PlacementAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false)
    @JoinColumn(name="subject_id")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    // Lưu "đề" dạng JSON để FE submit lại + backend grade
    // Ví dụ: [{"q":"...","options":["A","B"],"answer":"A","skill":"algebra"}]
    @Column(columnDefinition = "json", nullable = false)
    private String paperJson;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}
