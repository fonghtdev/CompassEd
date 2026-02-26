package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_progress")
@Getter @Setter
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private Long lessonId;

    @Column(nullable = false)
    private Boolean completed = false;

    private Integer score;
}
