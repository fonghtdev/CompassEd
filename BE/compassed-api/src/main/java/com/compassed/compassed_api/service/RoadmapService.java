package com.compassed.compassed_api.service;

import com.compassed.compassed_api.domain.entity.*;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapService {
    
    private final RoadmapRepository roadmapRepository;
    private final RoadmapModuleRepository roadmapModuleRepository;
    private final UserModuleProgressRepository userModuleProgressRepository;
    private final SubjectRepository subjectRepository;
    
    /**
     * Get roadmap by subject and level
     */
    public Roadmap getRoadmapBySubjectAndLevel(Long subjectId, String level) {
        Level levelEnum = Level.valueOf(level);
        return roadmapRepository.findBySubject_IdAndLevel(subjectId, levelEnum)
            .orElseThrow(() -> new RuntimeException("Roadmap not found for subject=" + subjectId + " level=" + level));
    }
    
    /**
     * Get modules by roadmap
     */
    public List<RoadmapModule> getModulesByRoadmap(Long roadmapId) {
        return roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmapId);
    }
    
    /**
     * Get module by ID
     */
    public RoadmapModule getModule(Long moduleId) {
        return roadmapModuleRepository.findById(moduleId)
            .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));
    }
    
    /**
     * Start module (create progress if not exists)
     */
    @Transactional
    public UserModuleProgress startModule(Long userId, Long moduleId) {
        Optional<UserModuleProgress> existing = userModuleProgressRepository.findByUserIdAndModuleId(userId, moduleId);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        UserModuleProgress progress = new UserModuleProgress();
        progress.setUserId(userId);
        progress.setModuleId(moduleId);
        progress.setStatus("IN_PROGRESS");
        progress.setProgressPercent(0);
        
        return userModuleProgressRepository.save(progress);
    }
    
    /**
     * Complete module
     */
    @Transactional
    public void completeModule(Long userId, Long moduleId, Integer miniTestScore) {
        UserModuleProgress progress = userModuleProgressRepository.findByUserIdAndModuleId(userId, moduleId)
            .orElseThrow(() -> new RuntimeException("Progress not found"));
        
        progress.setStatus("COMPLETED");
        progress.setProgressPercent(100);
        progress.setMiniTestScore(miniTestScore);
        progress.setCompletedAt(LocalDateTime.now());
        
        userModuleProgressRepository.save(progress);
        
        // Auto unlock next module
        unlockNextModule(userId, moduleId);
    }
    
    /**
     * Unlock next module
     */
    @Transactional
    public void unlockNextModule(Long userId, Long moduleId) {
        RoadmapModule currentModule = getModule(moduleId);
        
        // Find next module
        RoadmapModule nextModule = roadmapModuleRepository.findFirstByRoadmapIdAndOrderIndex(
            currentModule.getRoadmapId(), 
            currentModule.getOrderIndex() + 1
        );
        
        if (nextModule != null) {
            // Create progress for next module if not exists
            Optional<UserModuleProgress> nextProgress = userModuleProgressRepository.findByUserIdAndModuleId(userId, nextModule.getId());
            
            if (nextProgress.isEmpty()) {
                UserModuleProgress progress = new UserModuleProgress();
                progress.setUserId(userId);
                progress.setModuleId(nextModule.getId());
                progress.setStatus("NOT_STARTED");
                progress.setProgressPercent(0);
                
                userModuleProgressRepository.save(progress);
                log.info("Unlocked next module: {} for user: {}", nextModule.getId(), userId);
            }
        }
    }
    
    /**
     * Get user's progress for all modules
     */
    public List<UserModuleProgress> getUserProgress(Long userId) {
        return userModuleProgressRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Create roadmap (Admin only)
     */
    @Transactional
    public Roadmap createRoadmap(Long subjectId, String level, String title, String description) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new RuntimeException("Subject not found"));
        
        Level levelEnum = Level.valueOf(level);
        
        Roadmap roadmap = new Roadmap();
        roadmap.setSubject(subject);
        roadmap.setLevel(levelEnum);
        roadmap.setTitle(title);
        roadmap.setDescription(description);
        
        return roadmapRepository.save(roadmap);
    }
    
    /**
     * Create module (Admin only)
     */
    @Transactional
    public RoadmapModule createModule(Long roadmapId, String moduleName, Integer orderIndex, String content) {
        RoadmapModule module = new RoadmapModule();
        module.setRoadmapId(roadmapId);
        module.setModuleName(moduleName);
        module.setOrderIndex(orderIndex);
        module.setContent(content);
        
        return roadmapModuleRepository.save(module);
    }
}
