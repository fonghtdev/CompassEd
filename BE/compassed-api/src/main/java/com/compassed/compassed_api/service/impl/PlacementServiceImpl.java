package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.LinkedHashSet;

import org.springframework.context.annotation.Profile;
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
import com.compassed.compassed_api.domain.entity.UserProfile;
import com.compassed.compassed_api.domain.entity.UserSubjectFreeAttempt;
import com.compassed.compassed_api.domain.enums.AttemptStatus;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.repository.PlacementAttemptRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;
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
            UserProfileRepository userProfileRepository,
            UserSubjectFreeAttemptRepository freeAttemptRepository,
            SubscriptionRepository subscriptionRepository,
            PlacementAttemptRepository attemptRepository,
            PlacementResultRepository resultRepository,
            QuestionBankRepository questionBankRepository,
            ObjectMapper objectMapper,
            AiService aiService) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.freeAttemptRepository = freeAttemptRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.attemptRepository = attemptRepository;
        this.resultRepository = resultRepository;
        this.questionBankRepository = questionBankRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PlacementStartResponse startPlacement(Long userId, Long subjectId, Integer gradeLevel, String gradeBand) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found: " + subjectId));

        PlacementAttempt inProgress = attemptRepository
                .findTopByUser_IdAndSubject_IdAndStatusOrderByStartedAtDesc(userId, subjectId, AttemptStatus.IN_PROGRESS)
                .orElse(null);
        if (inProgress != null) {
            if (isLegacyDemoPaper(inProgress.getPaperJson())) {
                attemptRepository.delete(inProgress);
            } else {
            PlacementStartResponse resp = new PlacementStartResponse();
            resp.setAttemptId(inProgress.getId());
            resp.setSubjectId(subjectId);
            resp.setPaperJson(inProgress.getPaperJson());
            resp.setAnswersJson(inProgress.getAnswersJson());
            return resp;
            }
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
        if (canUseFree) {
            free.setUsed(true);
            free.setUsedAt(LocalDateTime.now());
            freeAttemptRepository.save(free);
        }

        String normalizedGradeBand = resolveUserGradeBand(userId, gradeBand, gradeLevel);
        int grade = resolveGradeLevel(gradeLevel, normalizedGradeBand);
        String paperJson = generatePlacementPaperJson(subject.getCode(), subjectId, grade, normalizedGradeBand);

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
            PlacementResult latest = resultRepository
                    .findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, attempt.getSubject().getId())
                    .orElseThrow(() -> new RuntimeException("Attempt already graded but result not found"));
            PlacementSubmitResponse already = new PlacementSubmitResponse();
            already.setScorePercent(latest.getScorePercent());
            already.setLevel(latest.getLevel() == null ? null : latest.getLevel().name());
            already.setSkillAnalysisJson(latest.getSkillAnalysisJson());
            already.setNextStep("GO_TO_RESULT");
            return already;
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
                String chosen = answers.get(id);
                if (chosen == null || chosen.isBlank()) {
                    continue;
                }
                List<String> options = extractOptions(q.get("options"));
                String normalizedChosen = normalizeAnswerToken(chosen, options);
                Set<String> correctSet = parseCorrectAnswers(String.valueOf(q.get("correct")), options);
                if (!normalizedChosen.isBlank() && correctSet.contains(normalizedChosen)) {
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

    private String generatePlacementPaperJson(String subjectCode, Long subjectId, int gradeLevel, String gradeBand) {
        try {
            List<Map<String, Object>> paper = new ArrayList<>(50);
            Set<Long> usedQuestionIds = new LinkedHashSet<>();

            addPlacementQuestionsStrict(
                    paper, usedQuestionIds, subjectId,
                    com.compassed.compassed_api.domain.QuestionBank.Level.L1,
                    gradeLevel, gradeBand, 20);
            addPlacementQuestionsStrict(
                    paper, usedQuestionIds, subjectId,
                    com.compassed.compassed_api.domain.QuestionBank.Level.L2,
                    gradeLevel, gradeBand, 20);
            addPlacementQuestionsStrict(
                    paper, usedQuestionIds, subjectId,
                    com.compassed.compassed_api.domain.QuestionBank.Level.L3,
                    gradeLevel, gradeBand, 10);

            if (paper.size() != 50) {
                throw new RuntimeException("QUESTION_BANK_INSUFFICIENT: Expected 50 questions but got " + paper.size());
            }
            Collections.shuffle(paper);
            return objectMapper.writeValueAsString(paper);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate placement paper: " + e.getMessage(), e);
        }
    }

    private void addPlacementQuestionsStrict(
            List<Map<String, Object>> paper,
            Set<Long> usedQuestionIds,
            Long subjectId,
            com.compassed.compassed_api.domain.QuestionBank.Level level,
            int gradeLevel,
            String gradeBand,
            int targetCount) {
        if (targetCount <= 0) return;

        List<com.compassed.compassed_api.domain.QuestionBank> pool = loadQuestionPool(subjectId, level, gradeLevel, gradeBand);
        List<com.compassed.compassed_api.domain.QuestionBank> available = new ArrayList<>();
        for (com.compassed.compassed_api.domain.QuestionBank row : pool) {
            if (row.getId() == null || usedQuestionIds.contains(row.getId())) continue;
            available.add(row);
        }
        if (available.size() < targetCount) {
            throw new RuntimeException(
                    "QUESTION_BANK_INSUFFICIENT: Need " + targetCount + " questions for " + level.name()
                            + ", but only " + available.size()
                            + " (subjectId=" + subjectId + ", gradeBand=" + gradeBand + ")");
        }

        Collections.shuffle(available);
        int added = 0;
        for (com.compassed.compassed_api.domain.QuestionBank row : available) {
            paper.add(toPlacementQuestion(row));
            usedQuestionIds.add(row.getId());
            added++;
            if (added >= targetCount) break;
        }
    }

    private List<com.compassed.compassed_api.domain.QuestionBank> loadQuestionPool(
            Long subjectId,
            com.compassed.compassed_api.domain.QuestionBank.Level level,
            int gradeLevel,
            String gradeBand) {
        String band = gradeBand == null ? "" : gradeBand.trim().toUpperCase(Locale.ROOT);
        List<com.compassed.compassed_api.domain.QuestionBank> allRows = questionBankRepository
                .findBySubjectIdAndLevelAndIsActiveTrue(subjectId, level);
        List<com.compassed.compassed_api.domain.QuestionBank> byBand = new ArrayList<>();
        for (com.compassed.compassed_api.domain.QuestionBank q : allRows) {
            String qBand = q.getGradeBand() == null ? "" : q.getGradeBand().trim().toUpperCase(Locale.ROOT);
            if (band.equals(qBand)) {
                byBand.add(q);
            }
        }

        if ("UNI_PREP".equals(band)) {
            // UNI_PREP can draw from both grade 11 and 12 pools.
            Map<Long, com.compassed.compassed_api.domain.QuestionBank> merged = new LinkedHashMap<>();
            for (com.compassed.compassed_api.domain.QuestionBank q : byBand) {
                if (q.getId() != null) merged.put(q.getId(), q);
            }
            for (com.compassed.compassed_api.domain.QuestionBank q : allRows) {
                Integer qGrade = q.getGradeLevel();
                if (qGrade != null && (qGrade == 11 || qGrade == 12) && q.getId() != null) {
                    merged.put(q.getId(), q);
                }
            }
            return new ArrayList<>(merged.values());
        }

        // Grade band 11/12: prioritize exact band first; fallback to exact grade level.
        if (!byBand.isEmpty()) {
            return byBand;
        }
        int targetGrade = "GRADE_12".equals(band) ? 12 : 11;
        Map<Long, com.compassed.compassed_api.domain.QuestionBank> strict = new LinkedHashMap<>();
        for (com.compassed.compassed_api.domain.QuestionBank q : allRows) {
            Integer qGrade = q.getGradeLevel();
            if (qGrade == null || qGrade != targetGrade) continue;
            if (q.getId() != null) strict.put(q.getId(), q);
        }
        return new ArrayList<>(strict.values());
    }

    private Map<String, Object> toPlacementQuestion(com.compassed.compassed_api.domain.QuestionBank qrow) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("id", qrow.getId());
        q.put("q", qrow.getQuestionText());
        List<String> options;
        try {
            options = objectMapper.readValue(qrow.getOptions(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            options = List.of("A. Option A", "B. Option B", "C. Option C", "D. Option D");
        }
        q.put("options", options);

        Set<String> normalizedCorrect = parseCorrectAnswers(qrow.getCorrectAnswer(), options);
        String canonicalCorrect = normalizedCorrect.isEmpty()
                ? "A"
                : String.join(",", normalizedCorrect);
        q.put("correct", canonicalCorrect);
        q.put("skill", qrow.getSkillType());
        return q;
    }

    private List<String> extractOptions(Object rawOptions) {
        if (rawOptions instanceof List<?> rawList) {
            List<String> out = new ArrayList<>();
            for (Object it : rawList) {
                out.add(String.valueOf(it == null ? "" : it));
            }
            return out;
        }
        return List.of();
    }

    private Set<String> parseCorrectAnswers(String correctRaw, List<String> options) {
        Set<String> out = new java.util.LinkedHashSet<>();
        if (correctRaw == null || correctRaw.isBlank()) {
            return out;
        }
        String[] parts = correctRaw.split("[,;/|]");
        for (String part : parts) {
            String token = normalizeAnswerToken(part, options);
            if (!token.isBlank()) {
                out.add(token);
            }
        }
        if (out.isEmpty()) {
            String token = normalizeAnswerToken(correctRaw, options);
            if (!token.isBlank()) out.add(token);
        }
        return out;
    }

    private String normalizeAnswerToken(String raw, List<String> options) {
        if (raw == null) return "";
        String token = raw.trim().toUpperCase(Locale.ROOT);
        if (token.isBlank()) return "";

        token = token.replace("OPTION_", "");

        if (token.matches("^[A-D]$")) return token;
        if (token.matches("^[1-4]$")) return String.valueOf((char) ('A' + Integer.parseInt(token) - 1));
        if (token.matches("^[0-3]$")) return String.valueOf((char) ('A' + Integer.parseInt(token)));
        if (token.matches("^[A-D][\\.|\\)|:|-]?.*$")) return String.valueOf(token.charAt(0));

        String normalizedText = normalizeOptionText(token);
        for (int i = 0; i < options.size() && i < 4; i++) {
            String option = String.valueOf(options.get(i));
            String optionNormalized = normalizeOptionText(option);
            if (!optionNormalized.isBlank() && optionNormalized.equals(normalizedText)) {
                return String.valueOf((char) ('A' + i));
            }
        }
        return "";
    }

    private String normalizeOptionText(String raw) {
        if (raw == null) return "";
        String text = raw.trim().toUpperCase(Locale.ROOT);
        text = text.replaceFirst("^[A-D][\\.|\\)|:|-]?\\s*", "");
        return text.replaceAll("\\s+", " ").trim();
    }

    private int resolveGradeLevel(Integer gradeLevel, String gradeBand) {
        if (gradeLevel != null && gradeLevel > 0) {
            return gradeLevel;
        }
        if ("GRADE_12".equals(gradeBand) || "UNI_PREP".equals(gradeBand)) {
            return 12;
        }
        return 11;
    }

    private String normalizeGradeBand(String gradeBand, Integer gradeLevel) {
        String raw = gradeBand == null ? "" : gradeBand.trim().toUpperCase();
        if ("GRADE_11".equals(raw) || "GRADE_12".equals(raw) || "UNI_PREP".equals(raw)) {
            return raw;
        }
        if (gradeLevel != null) {
            return gradeLevel >= 12 ? "GRADE_12" : "GRADE_11";
        }
        return "GRADE_11";
    }

    private String resolveUserGradeBand(Long userId, String gradeBand, Integer gradeLevel) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);
        if (profile != null) {
            String track = profile.getAcademicTrack() == null ? "" : profile.getAcademicTrack().trim().toUpperCase(Locale.ROOT);
            if ("GRADE_11".equals(track) || "GRADE_12".equals(track) || "UNI_PREP".equals(track)) {
                return track;
            }
        }
        return normalizeGradeBand(gradeBand, gradeLevel);
    }

    private boolean isLegacyDemoPaper(String paperJson) {
        if (paperJson == null || paperJson.isBlank()) return true;
        try {
            List<Map<String, Object>> items = objectMapper.readValue(
                    paperJson,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            if (items.size() != 50) return true;
            for (Map<String, Object> q : items) {
                String text = String.valueOf(q.getOrDefault("q", ""));
                if (text.toLowerCase(Locale.ROOT).contains("demo")) {
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            return true;
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
