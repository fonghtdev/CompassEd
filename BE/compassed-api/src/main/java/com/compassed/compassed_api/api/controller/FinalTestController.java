package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.domain.entity.FinalTest;
import com.compassed.compassed_api.domain.entity.FinalTestAttempt;
import com.compassed.compassed_api.service.FinalTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/final-tests")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FinalTestController {
    
    private final FinalTestService finalTestService;
    
    /**
     * Get final test by roadmap
     * GET /api/final-tests/roadmap/{roadmapId}
     */
    @GetMapping("/roadmap/{roadmapId}")
    public ResponseEntity<?> getFinalTest(@PathVariable Long roadmapId) {
        try {
            FinalTest finalTest = finalTestService.getFinalTestByRoadmap(roadmapId);
            return ResponseEntity.ok(finalTest);
            
        } catch (Exception e) {
            log.error("Error getting final test", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Submit final test
     * POST /api/final-tests/{finalTestId}/submit
     * Body: { "userId": 1, "answers": "[...]" }
     */
    @PostMapping("/{finalTestId}/submit")
    public ResponseEntity<?> submitFinalTest(@PathVariable Long finalTestId, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String answersJson = request.get("answers").toString();
            
            FinalTestAttempt attempt = finalTestService.submitFinalTest(userId, finalTestId, answersJson);
            
            return ResponseEntity.ok(Map.of(
                "attemptId", attempt.getId(),
                "score", attempt.getScore(),
                "passed", attempt.getPassed(),
                "message", attempt.getPassed() 
                    ? "Congratulations! You can now be promoted to the next level!" 
                    : "Please review and try again."
            ));
            
        } catch (Exception e) {
            log.error("Error submitting final test", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Promote user to next level
     * POST /api/final-tests/promote/{attemptId}
     */
    @PostMapping("/promote/{attemptId}")
    public ResponseEntity<?> promoteUser(@PathVariable Long attemptId) {
        try {
            finalTestService.promoteUser(attemptId);
            return ResponseEntity.ok(Map.of("message", "User promoted successfully!"));
            
        } catch (Exception e) {
            log.error("Error promoting user", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get user attempts
     * GET /api/final-tests/{finalTestId}/attempts?userId=1
     */
    @GetMapping("/{finalTestId}/attempts")
    public ResponseEntity<?> getUserAttempts(@PathVariable Long finalTestId, @RequestParam Long userId) {
        try {
            List<FinalTestAttempt> attempts = finalTestService.getUserAttempts(userId, finalTestId);
            return ResponseEntity.ok(attempts);
            
        } catch (Exception e) {
            log.error("Error getting user attempts", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
