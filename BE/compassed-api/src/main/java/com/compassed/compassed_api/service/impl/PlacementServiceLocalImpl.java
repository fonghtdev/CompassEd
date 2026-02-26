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
import com.compassed.compassed_api.domain.enums.AttemptStatus;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.local.LocalDataStore.PlacementAttemptMem;
import com.compassed.compassed_api.local.LocalDataStore.PlacementResultMem;
import com.compassed.compassed_api.local.LocalDataStore.SubjectInfo;
import com.compassed.compassed_api.service.PlacementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Profile("local")
public class PlacementServiceLocalImpl implements PlacementService {

    private final LocalDataStore localDataStore;
    private final ObjectMapper objectMapper;

    public PlacementServiceLocalImpl(LocalDataStore localDataStore, ObjectMapper objectMapper) {
        this.localDataStore = localDataStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public PlacementStartResponse startPlacement(Long userId, Long subjectId) {
        ensureUserExists(userId);
        SubjectInfo subject = localDataStore.getSubject(subjectId);
        if (subject == null) {
            throw new RuntimeException("Subject not found: " + subjectId);
        }

        boolean hasUsedFreeAttempt = localDataStore.hasUsedFreeAttempt(userId, subjectId);
        if (hasUsedFreeAttempt && !localDataStore.hasActiveSubscription(userId, subjectId)) {
            throw new RuntimeException("PAYMENT_REQUIRED: Need subscription to start placement");
        }
        if (!hasUsedFreeAttempt) {
            localDataStore.markFreeAttemptUsed(userId, subjectId);
        }

        String paperJson = generateDummyPaperJson(subject.code());
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
            throw new RuntimeException("Please create user via /api/dev/users first (local mode)");
        }
    }

    private Level decideLevel(double scorePercent) {
        if (scorePercent < 40.0) {
            return Level.L1;
        }
        if (scorePercent < 70.0) {
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

    private String generateDummyPaperJson(String subjectCode) {
        List<Map<String, Object>> paper = new ArrayList<>();
        String[] opts = new String[] { "A", "B", "C", "D" };
        Random rnd = new Random();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("id", i);
            q.put("q", "[" + subjectCode + "] Question " + i + " (demo)");
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
}
