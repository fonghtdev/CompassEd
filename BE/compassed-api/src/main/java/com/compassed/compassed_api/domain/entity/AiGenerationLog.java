package com.compassed.compassed_api.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_generation_logs")
@Getter
@Setter
public class AiGenerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String taskType;

    @Column(length = 60)
    private String subjectCode;

    @Column(nullable = false, columnDefinition = "longtext")
    private String inputPrompt;

    @Column(nullable = false, columnDefinition = "longtext")
    private String outputText;

    @Column(nullable = false, length = 30)
    private String reviewStatus = "PENDING";

    @Column(columnDefinition = "text")
    private String reviewNote;

    private Long reviewedByUserId;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;
}
