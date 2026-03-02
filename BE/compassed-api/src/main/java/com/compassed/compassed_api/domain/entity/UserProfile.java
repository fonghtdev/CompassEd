package com.compassed.compassed_api.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id" }))
@Getter
@Setter
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "text")
    private String learningGoal;

    private Integer targetScore;

    @Column(length = 20)
    private String academicTrack = "GRADE_11";

    @Column(nullable = false)
    private boolean notifyEmail = false;

    @Column(nullable = false)
    private boolean notifyInApp = true;

    private LocalDateTime updatedAt;
}
