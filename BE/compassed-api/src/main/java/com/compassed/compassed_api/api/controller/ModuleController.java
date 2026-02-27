package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.domain.entity.RoadmapModule;
import com.compassed.compassed_api.domain.entity.UserModuleProgress;
import com.compassed.compassed_api.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ModuleController {
    
    private final RoadmapService roadmapService;
    
    /**
     * Get module by ID
     * GET /api/modules/{moduleId}
     */
    @GetMapping("/{moduleId}")
    public ResponseEntity<?> getModule(@PathVariable Long moduleId) {
        try {
            RoadmapModule module = roadmapService.getModule(moduleId);
            return ResponseEntity.ok(module);
            
        } catch (Exception e) {
            log.error("Error getting module", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Start module
     * POST /api/modules/{moduleId}/start
     * Body: { "userId": 1 }
     */
    @PostMapping("/{moduleId}/start")
    public ResponseEntity<?> startModule(@PathVariable Long moduleId, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            UserModuleProgress progress = roadmapService.startModule(userId, moduleId);
            
            return ResponseEntity.ok(progress);
            
        } catch (Exception e) {
            log.error("Error starting module", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Complete module
     * POST /api/modules/{moduleId}/complete
     * Body: { "userId": 1, "miniTestScore": 85 }
     */
    @PostMapping("/{moduleId}/complete")
    public ResponseEntity<?> completeModule(@PathVariable Long moduleId, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Integer miniTestScore = request.get("miniTestScore") != null 
                ? Integer.parseInt(request.get("miniTestScore").toString()) 
                : null;
            
            roadmapService.completeModule(userId, moduleId, miniTestScore);
            
            return ResponseEntity.ok(Map.of("message", "Module completed successfully"));
            
        } catch (Exception e) {
            log.error("Error completing module", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get user's module progress
     * GET /api/modules/progress?userId=1
     */
    @GetMapping("/progress")
    public ResponseEntity<?> getUserProgress(@RequestParam Long userId) {
        try {
            List<UserModuleProgress> progress = roadmapService.getUserProgress(userId);
            return ResponseEntity.ok(progress);
            
        } catch (Exception e) {
            log.error("Error getting user progress", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
