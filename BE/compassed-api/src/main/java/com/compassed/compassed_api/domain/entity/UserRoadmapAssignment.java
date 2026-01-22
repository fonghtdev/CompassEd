package com.compassed.compassed_api.domain.entity;

import java.time.LocalDateTime;

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
        name = "user_roadmap_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"})
)
@Getter @Setter
public class UserRoadmapAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(optional=false)
    @JoinColumn(name="subject_id")
    private Subject subject;

    @ManyToOne(optional=false)
    @JoinColumn(name="roadmap_id")
    private Roadmap roadmap;

    private LocalDateTime assignedAt;
}
