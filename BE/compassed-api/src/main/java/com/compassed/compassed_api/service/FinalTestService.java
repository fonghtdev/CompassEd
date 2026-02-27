package com.compassed.compassed_api.service;

import com.compassed.compassed_api.domain.entity.FinalTest;
import com.compassed.compassed_api.domain.entity.FinalTestAttempt;
import com.compassed.compassed_api.repository.FinalTestAttemptRepository;
import com.compassed.compassed_api.repository.FinalTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinalTestService {
    
    private final FinalTestRepository finalTestRepository;
    private final FinalTestAttemptRepository finalTestAttemptRepository;
    
    /**
     * Get final test by roadmap
     */
    public FinalTest getFinalTestByRoadmap(Long roadmapId) {
        return finalTestRepository.findByRoadmapId(roadmapId)
            .orElseThrow(() -> new RuntimeException("Final test not found for roadmap: " + roadmapId));
    }
    
    /**
     * Submit final test
     */
    @Transactional
    public FinalTestAttempt submitFinalTest(Long userId, Long finalTestId, String answersJson) {
        FinalTest finalTest = finalTestRepository.findById(finalTestId)
            .orElseThrow(() -> new RuntimeException("Final test not found: " + finalTestId));
        
        // Calculate score
        int score = calculateScore(finalTest.getQuestionsJson(), answersJson);
        boolean passed = score >= finalTest.getPassThreshold();
        
        // Save attempt
        FinalTestAttempt attempt = new FinalTestAttempt();
        attempt.setUserId(userId);
        attempt.setFinalTestId(finalTestId);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setAnswersJson(answersJson);
        attempt.setPromoted(false); // Will be set to true if user chooses to promote
        
        return finalTestAttemptRepository.save(attempt);
    }
    
    /**
     * Promote user to next level
     */
    @Transactional
    public void promoteUser(Long attemptId) {
        FinalTestAttempt attempt = finalTestAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));
        
        if (!attempt.getPassed()) {
            throw new RuntimeException("Cannot promote - test not passed");
        }
        
        if (attempt.getPromoted()) {
            throw new RuntimeException("User already promoted");
        }
        
        // TODO: Update user's level in user_subjects table
        // For now, just mark as promoted
        attempt.setPromoted(true);
        finalTestAttemptRepository.save(attempt);
        
        log.info("User {} promoted after passing final test {}", attempt.getUserId(), attempt.getFinalTestId());
    }
    
    /**
     * Get user attempts
     */
    public List<FinalTestAttempt> getUserAttempts(Long userId, Long finalTestId) {
        return finalTestAttemptRepository.findByUserIdAndFinalTestIdOrderBySubmittedAtDesc(userId, finalTestId);
    }
    
    /**
     * Calculate score (simple implementation)
     */
    private int calculateScore(String questionsJson, String answersJson) {
        // TODO: Implement proper scoring logic
        return 80;
    }
    
    /**
     * Create final test (Admin only)
     */
    @Transactional
    public FinalTest createFinalTest(Long roadmapId, String title, String questionsJson, Integer passThreshold) {
        FinalTest finalTest = new FinalTest();
        finalTest.setRoadmapId(roadmapId);
        finalTest.setTitle(title);
        finalTest.setQuestionsJson(questionsJson);
        finalTest.setPassThreshold(passThreshold != null ? passThreshold : 75);
        
        return finalTestRepository.save(finalTest);
    }
}
