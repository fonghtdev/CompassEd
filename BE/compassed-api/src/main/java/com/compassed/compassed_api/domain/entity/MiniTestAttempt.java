package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mini_test_attempts")
@Data
public class MiniTestAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "mini_test_id", nullable = false)
    private Long miniTestId;
    
    @Column(nullable = false)
    private Integer score;
    
    @Column(nullable = false)
    private Boolean passed;
    
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @PrePersist
    protected void onSubmit() {
        submittedAt = LocalDateTime.now();
    }
}
