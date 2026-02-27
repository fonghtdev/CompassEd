package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.domain.entity.MiniTest;
import com.compassed.compassed_api.domain.entity.MiniTestAttempt;
import com.compassed.compassed_api.service.MiniTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mini-tests")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class MiniTestController {
    
    private final MiniTestService miniTestService;
    
    /**
     * Get mini test by module
     * GET /api/mini-tests/module/{moduleId}
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> getMiniTest(@PathVariable Long moduleId) {
        try {
            MiniTest miniTest = miniTestService.getMiniTestByModule(moduleId);
            return ResponseEntity.ok(miniTest);
            
        } catch (Exception e) {
            log.error("Error getting mini test", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Submit mini test
     * POST /api/mini-tests/{miniTestId}/submit
     * Body: { "userId": 1, "answers": "[...]" }
     */
    @PostMapping("/{miniTestId}/submit")
    public ResponseEntity<?> submitMiniTest(@PathVariable Long miniTestId, @RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String answersJson = request.get("answers").toString();
            
            MiniTestAttempt attempt = miniTestService.submitMiniTest(userId, miniTestId, answersJson);
            
            return ResponseEntity.ok(Map.of(
                "attemptId", attempt.getId(),
                "score", attempt.getScore(),
                "passed", attempt.getPassed(),
                "message", attempt.getPassed() ? "Congratulations! You passed!" : "Please try again."
            ));
            
        } catch (Exception e) {
            log.error("Error submitting mini test", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get user attempts
     * GET /api/mini-tests/{miniTestId}/attempts?userId=1
     */
    @GetMapping("/{miniTestId}/attempts")
    public ResponseEntity<?> getUserAttempts(@PathVariable Long miniTestId, @RequestParam Long userId) {
        try {
            List<MiniTestAttempt> attempts = miniTestService.getUserAttempts(userId, miniTestId);
            return ResponseEntity.ok(attempts);
            
        } catch (Exception e) {
            log.error("Error getting user attempts", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
