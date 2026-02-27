package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "final_test_attempts")
@Data
public class FinalTestAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "final_test_id", nullable = false)
    private Long finalTestId;
    
    @Column(nullable = false)
    private Integer score;
    
    @Column(nullable = false)
    private Boolean passed;
    
    @Column(nullable = false)
    private Boolean promoted = false;
    
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @PrePersist
    protected void onSubmit() {
        submittedAt = LocalDateTime.now();
    }
}
