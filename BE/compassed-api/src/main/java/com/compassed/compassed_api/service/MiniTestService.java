package com.compassed.compassed_api.service;

import com.compassed.compassed_api.domain.entity.MiniTest;
import com.compassed.compassed_api.domain.entity.MiniTestAttempt;
import com.compassed.compassed_api.repository.MiniTestAttemptRepository;
import com.compassed.compassed_api.repository.MiniTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MiniTestService {
    
    private final MiniTestRepository miniTestRepository;
    private final MiniTestAttemptRepository miniTestAttemptRepository;
    private final RoadmapService roadmapService;
    
    /**
     * Get mini test by module ID
     */
    public MiniTest getMiniTestByModule(Long moduleId) {
        List<MiniTest> tests = miniTestRepository.findByModuleId(moduleId);
        if (tests.isEmpty()) {
            throw new RuntimeException("Mini test not found for module: " + moduleId);
        }
        return tests.get(0);
    }
    
    /**
     * Submit mini test
     */
    @Transactional
    public MiniTestAttempt submitMiniTest(Long userId, Long miniTestId, String answersJson) {
        MiniTest miniTest = miniTestRepository.findById(miniTestId)
            .orElseThrow(() -> new RuntimeException("Mini test not found: " + miniTestId));
        
        // Calculate score (simple implementation - should be more sophisticated)
        int score = calculateScore(miniTest.getQuestionsJson(), answersJson);
        boolean passed = score >= miniTest.getPassThreshold();
        
        // Save attempt
        MiniTestAttempt attempt = new MiniTestAttempt();
        attempt.setUserId(userId);
        attempt.setMiniTestId(miniTestId);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setAnswersJson(answersJson);
        
        attempt = miniTestAttemptRepository.save(attempt);
        
        // If passed, complete module and unlock next
        if (passed) {
            roadmapService.completeModule(userId, miniTest.getModuleId(), score);
        }
        
        return attempt;
    }
    
    /**
     * Get user's attempts for a mini test
     */
    public List<MiniTestAttempt> getUserAttempts(Long userId, Long miniTestId) {
        return miniTestAttemptRepository.findByUserIdAndMiniTestIdOrderBySubmittedAtDesc(userId, miniTestId);
    }
    
    /**
     * Calculate score (simple implementation)
     */
    private int calculateScore(String questionsJson, String answersJson) {
        // TODO: Implement proper scoring logic
        // For now, return a mock score
        return 75;
    }
    
    /**
     * Create mini test (Admin only)
     */
    @Transactional
    public MiniTest createMiniTest(Long moduleId, String title, String questionsJson, Integer passThreshold) {
        MiniTest miniTest = new MiniTest();
        miniTest.setModuleId(moduleId);
        miniTest.setTitle(title);
        miniTest.setQuestionsJson(questionsJson);
        miniTest.setPassThreshold(passThreshold != null ? passThreshold : 70);
        
        return miniTestRepository.save(miniTest);
    }
}
