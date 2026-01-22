package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_subject_free_attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"})
)
@Getter @Setter
public class UserSubjectFreeAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false)
    @JoinColumn(name="subject_id")
    private Subject subject;

    @Column(nullable=false)
    private boolean used;

    private LocalDateTime usedAt;
}
