package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_module_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "module_id"})
})
@Data
public class UserModuleProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "module_id", nullable = false)
    private Long moduleId;
    
    @Column(length = 50)
    private String status = "NOT_STARTED"; // NOT_STARTED, IN_PROGRESS, COMPLETED
    
    @Column(name = "progress_percent")
    private Integer progressPercent = 0;
    
    @Column(name = "mini_test_score")
    private Integer miniTestScore;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
