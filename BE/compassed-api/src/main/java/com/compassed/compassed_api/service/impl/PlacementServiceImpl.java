package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.PlacementStartResponse;
import com.compassed.compassed_api.api.dto.PlacementSubmitRequest;
import com.compassed.compassed_api.api.dto.PlacementSubmitResponse;
import com.compassed.compassed_api.domain.entity.PlacementAttempt;
import com.compassed.compassed_api.domain.entity.PlacementResult;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.entity.UserSubjectFreeAttempt;
import com.compassed.compassed_api.domain.enums.AttemptStatus;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.repository.PlacementAttemptRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserSubjectFreeAttemptRepository;
import com.compassed.compassed_api.service.AiService;
import com.compassed.compassed_api.service.PlacementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Profile("mysql")
public class PlacementServiceImpl implements PlacementService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final UserSubjectFreeAttemptRepository freeAttemptRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlacementAttemptRepository attemptRepository;
    private final PlacementResultRepository resultRepository;
    private final QuestionBankRepository questionBankRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public PlacementServiceImpl(
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            UserSubjectFreeAttemptRepository freeAttemptRepository,
            SubscriptionRepository subscriptionRepository,
            PlacementAttemptRepository attemptRepository,
            PlacementResultRepository resultRepository,
            QuestionBankRepository questionBankRepository,
            ObjectMapper objectMapper,
            AiService aiService) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.freeAttemptRepository = freeAttemptRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.attemptRepository = attemptRepository;
        this.resultRepository = resultRepository;
        this.questionBankRepository = questionBankRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PlacementStartResponse startPlacement(Long userId, Long subjectId, Integer gradeLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found: " + subjectId));

        // 1) Check FREE 1 lần / môn
        UserSubjectFreeAttempt free = freeAttemptRepository
                .findByUserIdAndSubjectId(userId, subjectId)
                .orElseGet(() -> {
                    UserSubjectFreeAttempt x = new UserSubjectFreeAttempt();
                    x.setUser(user);
                    x.setSubject(subject);
                    x.setUsed(false);
                    return x;
                });

        boolean canUseFree = !free.isUsed();

        // 2) Nếu hết FREE: yêu cầu đã mua subscription mới được làm placement
        if (!canUseFree) {
            boolean hasSub = subscriptionRepository.existsByUserIdAndSubjectIdAndIsActiveTrue(userId, subjectId);
            if (!hasSub) {
                throw new RuntimeException("PAYMENT_REQUIRED: Need subscription to start placement");
            }
        } else {
            free.setUsed(true);
            free.setUsedAt(LocalDateTime.now());
            freeAttemptRepository.save(free);
        }

        int grade = gradeLevel != null ? gradeLevel : 10;

        // 3) Tạo đề (V1: lấy L1 theo khối lớp, fallback dummy)
        String paperJson = generatePlacementPaperJson(subject.getCode(), subjectId, grade);

        PlacementAttempt attempt = new PlacementAttempt();
        attempt.setUser(user);
        attempt.setSubject(subject);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setPaperJson(paperJson);
        attempt.setStartedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        PlacementStartResponse resp = new PlacementStartResponse();
        resp.setAttemptId(attempt.getId());
        resp.setSubjectId(subjectId);
        resp.setPaperJson(paperJson);
        return resp;
    }

    @Override
    public PlacementSubmitResponse submitPlacement(Long userId, Long attemptId, PlacementSubmitRequest request) {
        PlacementAttempt attempt = attemptRepository.findByIdAndUser_Id(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getStatus() == AttemptStatus.GRADED) {
            throw new RuntimeException("Attempt already graded");
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        // Grade (V1: so sánh answersJson với paperJson)
        double scorePercent = gradePercent(attempt.getPaperJson(), request.getAnswersJson());
        Level level = decideLevel(scorePercent);

        // AI phân tích kỹ năng (từ paper+answers)
        String skillAnalysis = aiService.analyzeSkills(
                attempt.getSubject().getCode(),
                attempt.getPaperJson(),
                request.getAnswersJson());

        // Save result (dù user không subscribe)
        PlacementResult result = new PlacementResult();
        result.setUser(attempt.getUser());
        result.setSubject(attempt.getSubject());
        result.setScorePercent(scorePercent);
        result.setLevel(level);
        result.setSkillAnalysisJson(skillAnalysis);
        result.setCreatedAt(LocalDateTime.now());
        resultRepository.save(result);

        attempt.setStatus(AttemptStatus.GRADED);
        attemptRepository.save(attempt);

        PlacementSubmitResponse resp = new PlacementSubmitResponse();
        resp.setScorePercent(scorePercent);
        resp.setLevel(level.name());
        resp.setSkillAnalysisJson(skillAnalysis);
        resp.setNextStep("SUBSCRIBE_TO_UNLOCK_ROADMAP");
        return resp;
    }

    private Level decideLevel(double scorePercent) {
        if (scorePercent < 50.0)
            return Level.L1;
        if (scorePercent < 90.0)
            return Level.L2;
        return Level.L3;
    }

    /**
     * paperJson: [{ "id":1,"correct":"A",... }]
     * answersJson: { "1":"A", "2":"B" ... } (V1 format)
     */
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

            if (total == 0)
                return 0.0;
            return (correctCount * 100.0) / total;
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format for grading: " + e.getMessage());
        }
    }

    private String generatePlacementPaperJson(String subjectCode, Long subjectId, int gradeLevel) {
        try {
            List<Map<String, Object>> paper = new ArrayList<>();
            
            // Convert gradeLevel (10/11/12) to className format ("lớp 10"/"lớp 11"/"lớp 12")
            String className = "lớp " + gradeLevel;
            
            // Random 20 câu Level 1, 20 câu Level 2, 10 câu Level 3
            // Level 1: 20 câu
            var l1Questions = questionBankRepository.findRandomQuestions(subjectId, "L1", className, PageRequest.of(0, 20));
            addQuestionsToPaper(paper, l1Questions);
            
            // Level 2: 20 câu
            var l2Questions = questionBankRepository.findRandomQuestions(subjectId, "L2", className, PageRequest.of(0, 20));
            addQuestionsToPaper(paper, l2Questions);
            
            // Level 3: 10 câu
            var l3Questions = questionBankRepository.findRandomQuestions(subjectId, "L3", className, PageRequest.of(0, 10));
            addQuestionsToPaper(paper, l3Questions);
            
            // Shuffle để trộn các câu hỏi
            java.util.Collections.shuffle(paper);
            
            if (!paper.isEmpty()) {
                return objectMapper.writeValueAsString(paper);
            }
        } catch (Exception e) {
            // fallback below
        }
        return generateDummyPaperJson(subjectCode, gradeLevel);
    }
    
    private void addQuestionsToPaper(List<Map<String, Object>> paper, List<?> questions) {
        if (questions != null && !questions.isEmpty()) {
            for (var qrow : questions) {
                // Cast to QuestionBank entity
                com.compassed.compassed_api.domain.QuestionBank q = (com.compassed.compassed_api.domain.QuestionBank) qrow;
                Map<String, Object> questionMap = new LinkedHashMap<>();
                questionMap.put("id", q.getId());
                questionMap.put("q", q.getQuestionText());
                // Build options array from separate option fields
                List<String> options = new ArrayList<>();
                if (q.getOptionA() != null) options.add(q.getOptionA());
                if (q.getOptionB() != null) options.add(q.getOptionB());
                if (q.getOptionC() != null) options.add(q.getOptionC());
                if (q.getOptionD() != null) options.add(q.getOptionD());
                questionMap.put("options", options);
                questionMap.put("correct", q.getCorrectAnswer());
                questionMap.put("skill", q.getSkillTag()); // Changed from getSkillType()
                questionMap.put("level", q.getLevel().toString());
                paper.add(questionMap);
            }
        }
    }

    private String generateDummyPaperJson(String subjectCode, int gradeLevel) {
        // V1: tạo 50 câu dummy, mỗi câu có correct answer
        // FE sẽ render theo field q/options/id
        List<Map<String, Object>> paper = new ArrayList<>();
        String[] opts = new String[] { "A", "B", "C", "D" };

        Random rnd = new Random();
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("id", i);
            q.put("q", "[" + subjectCode + " | Lớp " + gradeLevel + "] Câu " + i + ": chọn đáp án đúng (demo)");
            q.put("options", List.of(
                    "A. Đáp án A",
                    "B. Đáp án B",
                    "C. Đáp án C",
                    "D. Đáp án D"));
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found: " + subjectId));
        
        UserSubjectFreeAttempt free = freeAttemptRepository
                .findByUserIdAndSubjectId(userId, subjectId)
                .orElseGet(() -> {
                    UserSubjectFreeAttempt x = new UserSubjectFreeAttempt();
                    x.setUser(user);
                    x.setSubject(subject);
                    x.setUsed(false);
                    return x;
                });
        
        return free.isUsed() ? 0 : 1;
    }
    
    @Override
    public void decrementFreeAttempts(Long userId, Long subjectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found: " + subjectId));
        
        UserSubjectFreeAttempt free = freeAttemptRepository
                .findByUserIdAndSubjectId(userId, subjectId)
                .orElseGet(() -> {
                    UserSubjectFreeAttempt x = new UserSubjectFreeAttempt();
                    x.setUser(user);
                    x.setSubject(subject);
                    x.setUsed(false);
                    return x;
                });
        
        if (!free.isUsed()) {
            free.setUsed(true);
            free.setUsedAt(LocalDateTime.now());
            freeAttemptRepository.save(free);
        }
    }
}
