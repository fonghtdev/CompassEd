package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "mini_tests")
@Data
public class MiniTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "module_id", nullable = false)
    private Long moduleId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(name = "questions_json", columnDefinition = "TEXT", nullable = false)
    private String questionsJson;
    
    @Column(name = "pass_threshold")
    private Integer passThreshold = 70;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
