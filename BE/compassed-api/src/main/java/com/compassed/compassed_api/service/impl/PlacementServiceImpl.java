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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(PlacementServiceImpl.class);

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

        PlacementAttempt inProgress = attemptRepository
                .findTopByUser_IdAndSubject_IdAndStatusOrderByStartedAtDesc(userId, subjectId, AttemptStatus.IN_PROGRESS)
                .orElse(null);
        if (inProgress != null) {
            PlacementStartResponse resp = new PlacementStartResponse();
            resp.setAttemptId(inProgress.getId());
            resp.setSubjectId(subjectId);
            resp.setPaperJson(inProgress.getPaperJson());
            resp.setAnswersJson(inProgress.getAnswersJson());
            return resp;
        }

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
        String paperJson = generatePlacementPaperJson(subject.getCode(), subjectId, grade);

        PlacementAttempt attempt = new PlacementAttempt();
        attempt.setUser(user);
        attempt.setSubject(subject);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setPaperJson(paperJson);
        attempt.setAnswersJson("{}");
        attempt.setStartedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        PlacementStartResponse resp = new PlacementStartResponse();
        resp.setAttemptId(attempt.getId());
        resp.setSubjectId(subjectId);
        resp.setPaperJson(paperJson);
        resp.setAnswersJson(attempt.getAnswersJson());
        return resp;
    }

    @Override
    public void saveProgress(Long userId, Long attemptId, PlacementSubmitRequest request) {
        PlacementAttempt attempt = attemptRepository.findByIdAndUser_Id(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Attempt is not in progress");
        }

        String answersJson = request == null ? null : request.getAnswersJson();
        if (answersJson == null || answersJson.isBlank()) {
            throw new RuntimeException("answersJson is required");
        }

        attempt.setAnswersJson(answersJson);
        attemptRepository.save(attempt);
    }

    @Override
    public PlacementSubmitResponse submitPlacement(Long userId, Long attemptId, PlacementSubmitRequest request) {
        PlacementAttempt attempt = attemptRepository.findByIdAndUser_Id(attemptId, userId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getStatus() == AttemptStatus.GRADED) {
            throw new RuntimeException("Attempt already graded");
        }

        String answersJson = request == null ? null : request.getAnswersJson();
        if (answersJson == null || answersJson.isBlank()) {
            answersJson = attempt.getAnswersJson();
        }
        if (answersJson == null || answersJson.isBlank()) {
            throw new RuntimeException("answersJson is required");
        }

        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setAnswersJson(answersJson);
        attemptRepository.save(attempt);

        double scorePercent = gradePercent(attempt.getPaperJson(), answersJson);
        Level level = decideLevel(scorePercent);

        String skillAnalysis;
        try {
            skillAnalysis = aiService.analyzeSkills(
                    attempt.getSubject().getCode(),
                    attempt.getPaperJson(),
                    answersJson);
        } catch (Exception ex) {
            log.warn("AI analyzeSkills failed for attemptId={}, fallback to local summary. cause={}",
                    attemptId, ex.getMessage());
            skillAnalysis = fallbackSkillAnalysis(scorePercent, level);
        }

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
        if (scorePercent < 50.0) {
            return Level.L1;
        }
        if (scorePercent < 90.0) {
            return Level.L2;
        }
        return Level.L3;
    }

    private String fallbackSkillAnalysis(double scorePercent, Level level) {
        String overall = level == Level.L3 ? "good" : level == Level.L2 ? "average" : "weak";
        return """
                {"mode":"FALLBACK","overall_level":"%s","skills":[],"weak_topics":[],"recommendations":["Continue with your roadmap based on placement level."],"scorePercent":%.2f}
                """.formatted(overall, scorePercent);
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

            if (total == 0) {
                return 0.0;
            }
            return (correctCount * 100.0) / total;
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON format for grading: " + e.getMessage());
        }
    }

    private String generatePlacementPaperJson(String subjectCode, Long subjectId, int gradeLevel) {
        try {
            List<Map<String, Object>> paper = new ArrayList<>();
            var rows = questionBankRepository.findRandomQuestions(subjectId, "L1", gradeLevel, PageRequest.of(0, 50));
            if (rows != null && !rows.isEmpty()) {
                for (var qrow : rows) {
                    Map<String, Object> q = new LinkedHashMap<>();
                    q.put("id", qrow.getId());
                    q.put("q", qrow.getQuestionText());
                    try {
                        List<String> options = objectMapper.readValue(qrow.getOptions(), new TypeReference<List<String>>() {
                        });
                        q.put("options", options);
                    } catch (Exception e) {
                        q.put("options", List.of("A. Option A", "B. Option B", "C. Option C", "D. Option D"));
                    }
                    q.put("correct", qrow.getCorrectAnswer());
                    q.put("skill", qrow.getSkillType());
                    paper.add(q);
                }
                return objectMapper.writeValueAsString(paper);
            }
        } catch (Exception e) {
            // fallback below
        }
        return generateDummyPaperJson(subjectCode, gradeLevel);
    }

    private String generateDummyPaperJson(String subjectCode, int gradeLevel) {
        List<Map<String, Object>> paper = new ArrayList<>();
        String[] opts = new String[] { "A", "B", "C", "D" };

        Random rnd = new Random();
        for (int i = 1; i <= 50; i++) {
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("id", i);
            q.put("q", "[" + subjectCode + " | Grade " + gradeLevel + "] Question " + i);
            q.put("options", List.of(
                    "A. Option A",
                    "B. Option B",
                    "C. Option C",
                    "D. Option D"));
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
