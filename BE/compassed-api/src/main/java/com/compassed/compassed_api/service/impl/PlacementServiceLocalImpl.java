package com.compassed.compassed_api.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.PlacementStartResponse;
import com.compassed.compassed_api.api.dto.PlacementSubmitRequest;
import com.compassed.compassed_api.api.dto.PlacementSubmitResponse;
import com.compassed.compassed_api.domain.QuestionBank;
import com.compassed.compassed_api.domain.enums.AttemptStatus;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.local.LocalDataStore.PlacementAttemptMem;
import com.compassed.compassed_api.local.LocalDataStore.PlacementResultMem;
import com.compassed.compassed_api.local.LocalDataStore.SubjectInfo;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.service.PlacementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Profile("local")
public class PlacementServiceLocalImpl implements PlacementService {

    private final LocalDataStore localDataStore;
    private final ObjectMapper objectMapper;
    private final QuestionBankRepository questionBankRepository;

    public PlacementServiceLocalImpl(LocalDataStore localDataStore, ObjectMapper objectMapper, QuestionBankRepository questionBankRepository) {
        this.localDataStore = localDataStore;
        this.objectMapper = objectMapper;
        this.questionBankRepository = questionBankRepository;
    }

    @Override
    public PlacementStartResponse startPlacement(Long userId, Long subjectId, Integer gradeLevel) {
        ensureUserExists(userId);
        SubjectInfo subject = localDataStore.getSubject(subjectId);
        if (subject == null) {
            throw new RuntimeException("Subject not found: " + subjectId);
        }

        int grade = gradeLevel != null ? gradeLevel : 10;
        // Kiểm tra xem có câu hỏi nào trong QuestionBank không
        List<QuestionBank> questions = questionBankRepository.findBySubjectIdAndLevelAndGradeLevelAndIsActiveTrue(
                subjectId, QuestionBank.Level.L1, grade);
        if (questions.isEmpty()) {
            throw new RuntimeException("No questions found for subject: " + subjectId);
        }

        String paperJson = generateDummyPaperJson(subject.code(), grade);
        PlacementAttemptMem attempt = new PlacementAttemptMem();
        attempt.setId(localDataStore.nextAttemptId());
        attempt.setUserId(userId);
        attempt.setSubjectId(subjectId);
        attempt.setPaperJson(paperJson);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now());
        localDataStore.saveAttempt(attempt);

        PlacementStartResponse resp = new PlacementStartResponse();
        resp.setAttemptId(attempt.getId());
        resp.setSubjectId(subjectId);
        resp.setPaperJson(paperJson);
        return resp;
    }

    @Override
    public PlacementSubmitResponse submitPlacement(Long userId, Long attemptId, PlacementSubmitRequest request) {
        PlacementAttemptMem attempt = localDataStore.getAttempt(attemptId);
        if (attempt == null || !attempt.getUserId().equals(userId)) {
            throw new RuntimeException("Attempt not found");
        }
        if (attempt.getStatus() == AttemptStatus.GRADED) {
            throw new RuntimeException("Attempt already graded");
        }

        double scorePercent = gradePercent(attempt.getPaperJson(), request.getAnswersJson());
        Level level = decideLevel(scorePercent);
        String skillAnalysis = """
                {"mode":"LOCAL","skills":[],"weak_topics":[],"recommendations":["Code-first local mode: no DB"]}
                """;

        attempt.setStatus(AttemptStatus.GRADED);
        PlacementResultMem result = new PlacementResultMem();
        result.setLevel(level);
        result.setScorePercent(scorePercent);
        result.setSkillAnalysisJson(skillAnalysis);
        localDataStore.saveLatestResult(userId, attempt.getSubjectId(), result);
        localDataStore.recordPlacementHistory(
                userId,
                attempt.getId(),
                attempt.getSubjectId(),
                scorePercent,
                level.name());

        PlacementSubmitResponse resp = new PlacementSubmitResponse();
        resp.setScorePercent(scorePercent);
        resp.setLevel(level.name());
        resp.setSkillAnalysisJson(skillAnalysis);
        resp.setNextStep("SUBSCRIBE_TO_UNLOCK_ROADMAP");
        return resp;
    }

    private void ensureUserExists(Long userId) {
        if (!localDataStore.userExists(userId)) {
            // Auto-create user if not exists
            localDataStore.getOrCreateUser(userId, "user" + userId + "@test.com", "Test User " + userId);
        }
    }

    private Level decideLevel(double scorePercent) {
        if (scorePercent < 50.0) {
            return Level.L1;
        }
        if (scorePercent < 90.0) {
            return Level.L2;
        }
        return Level.L3;
    }

    private double gradePercent(String paperJson, String answersJson) {
        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                    paperJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            Map<String, String> answers = objectMapper.readValue(
                    answersJson,
                    new TypeReference<Map<String, String>>() {
                    });
            int total = items.size();
            int correctCount = 0;
            for (Map<String, Object> q : items) {
                String id = String.valueOf(q.get("id"));
                String correct = String.valueOf(q.get("correct"));
                String chosen = answers.get(id);
                if (chosen != null && chosen.equalsIgnoreCase(correct)) {
                    correctCount++;
                }
            }
            return total == 0 ? 0.0 : (correctCount * 100.0) / total;
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format for grading: " + e.getMessage());
        }
    }

    private String generateDummyPaperJson(String subjectCode, int gradeLevel) {
        // Lấy câu hỏi từ database thay vì hardcode
        List<QuestionBank> questions = questionBankRepository.findBySubjectIdAndLevelAndGradeLevelAndIsActiveTrue(
            getSubjectIdByCode(subjectCode), com.compassed.compassed_api.domain.QuestionBank.Level.L1, gradeLevel);
        
        if (questions.isEmpty()) {
            // Fallback to dummy if no questions in DB
            return generateFallbackPaperJson(subjectCode, gradeLevel);
        }
        
        // Chọn random 50 câu (hoặc ít hơn nếu không đủ)
        List<QuestionBank> selectedQuestions = selectRandomQuestions(questions, 50);
        
        List<Map<String, Object>> paper = new ArrayList<>();
        for (QuestionBank q : selectedQuestions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", q.getId());
            item.put("q", q.getQuestionText());
            
            // Parse options từ JSON string
            try {
                List<String> options = objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {});
                item.put("options", options);
            } catch (Exception e) {
                item.put("options", List.of("A. Option A", "B. Option B", "C. Option C", "D. Option D"));
            }
            
            item.put("correct", q.getCorrectAnswer());
            item.put("skill", q.getSkillType());
            paper.add(item);
        }

        // Nếu chưa đủ 50 câu, bổ sung câu hỏi dummy để đủ số lượng
        if (paper.size() < 50) {
            long maxId = 0L;
            for (QuestionBank q : selectedQuestions) {
                if (q.getId() != null && q.getId() > maxId) {
                    maxId = q.getId();
                }
            }
            String[] opts = new String[] { "A", "B", "C", "D" };
            Random rnd = new Random();
            int remain = 50 - paper.size();
            for (int i = 1; i <= remain; i++) {
                Map<String, Object> extra = new LinkedHashMap<>();
                extra.put("id", maxId + i);
                extra.put("q", "[" + subjectCode + "] Câu bổ sung " + i + " (demo)");
                extra.put("options", List.of("A. Option A", "B. Option B", "C. Option C", "D. Option D"));
                extra.put("correct", opts[rnd.nextInt(opts.length)]);
                extra.put("skill", "topic_demo");
                paper.add(extra);
            }
        }
        
        try {
            return objectMapper.writeValueAsString(paper);
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate paper json");
        }
    }
    
    private Long getSubjectIdByCode(String code) {
        // Math=1, Literature=2, English=3
        return switch (code.toUpperCase()) {
            case "MATH", "MATHEMATICS" -> 1L;
            case "LIT", "LITERATURE" -> 2L;
            case "ENG", "ENGLISH" -> 3L;
            default -> 1L;
        };
    }
    
    private List<QuestionBank> selectRandomQuestions(List<QuestionBank> questions, int count) {
        if (questions.size() <= count) {
            return questions;
        }
        List<QuestionBank> shuffled = new ArrayList<>(questions);
        java.util.Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }
    
    private String generateFallbackPaperJson(String subjectCode, int gradeLevel) {
        List<Map<String, Object>> paper = new ArrayList<>();
        String[] opts = new String[] { "A", "B", "C", "D" };
        Random rnd = new Random();
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("id", i);
            q.put("q", "[" + subjectCode + " | Lớp " + gradeLevel + "] Question " + i + " (demo)");
            q.put("options", List.of("A. Option A", "B. Option B", "C. Option C", "D. Option D"));
            q.put("correct", opts[rnd.nextInt(opts.length)]);
            q.put("skill", "topic_demo");
            paper.add(q);
        }
        try {
            return objectMapper.writeValueAsString(paper);
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate paper json");
        }
    }
    
    @Override
    public int checkFreeAttempts(Long userId, Long subjectId) {
        ensureUserExists(userId);
        boolean hasUsedFree = localDataStore.hasUsedFreeAttempt(userId, subjectId);
        return hasUsedFree ? 0 : 1;
    }
    
    @Override
    public void decrementFreeAttempts(Long userId, Long subjectId) {
        ensureUserExists(userId);
        localDataStore.markFreeAttemptUsed(userId, subjectId);
    }
}
