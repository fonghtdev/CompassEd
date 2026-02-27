package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.domain.entity.Roadmap;
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
@RequestMapping("/api/roadmaps")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class RoadmapController {
    
    private final RoadmapService roadmapService;
    
    /**
     * Get roadmap by subject and level
     * GET /api/roadmaps/{subjectId}/{level}
     */
    @GetMapping("/{subjectId}/{level}")
    public ResponseEntity<?> getRoadmap(@PathVariable Long subjectId, @PathVariable String level) {
        try {
            Roadmap roadmap = roadmapService.getRoadmapBySubjectAndLevel(subjectId, level);
            List<RoadmapModule> modules = roadmapService.getModulesByRoadmap(roadmap.getId());
            
            return ResponseEntity.ok(Map.of(
                "roadmap", roadmap,
                "modules", modules
            ));
            
        } catch (Exception e) {
            log.error("Error getting roadmap", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get modules by roadmap
     * GET /api/roadmaps/{roadmapId}/modules
     */
    @GetMapping("/{roadmapId}/modules")
    public ResponseEntity<?> getModules(@PathVariable Long roadmapId) {
        try {
            List<RoadmapModule> modules = roadmapService.getModulesByRoadmap(roadmapId);
            return ResponseEntity.ok(modules);
            
        } catch (Exception e) {
            log.error("Error getting modules", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Create roadmap (Admin only)
     * POST /api/roadmaps
     * Body: { "subjectId": 1, "level": "L1", "title": "Math L1", "description": "..." }
     */
    @PostMapping
    public ResponseEntity<?> createRoadmap(@RequestBody Map<String, Object> request) {
        try {
            Long subjectId = Long.parseLong(request.get("subjectId").toString());
            String level = request.get("level").toString();
            String title = request.get("title").toString();
            String description = request.get("description").toString();
            
            Roadmap roadmap = roadmapService.createRoadmap(subjectId, level, title, description);
            return ResponseEntity.ok(roadmap);
            
        } catch (Exception e) {
            log.error("Error creating roadmap", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
