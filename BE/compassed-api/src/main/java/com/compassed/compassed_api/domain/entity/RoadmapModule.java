package com.compassed.compassed_api.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap_modules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"roadmap_id", "order_index"})
})
@Data
public class RoadmapModule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "roadmap_id", nullable = false)
    private Long roadmapId;
    
    @Column(name = "module_name", nullable = false)
    private String moduleName;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
