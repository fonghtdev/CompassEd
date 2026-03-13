package com.compassed.compassed_api.api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.AiGeneratedRoadmapResponse;
import com.compassed.compassed_api.api.dto.AiRoadmapLessonResponse;
import com.compassed.compassed_api.api.dto.ChangeMyPasswordRequest;
import com.compassed.compassed_api.api.dto.UpdateMyProfileRequest;
import com.compassed.compassed_api.domain.entity.PlacementResult;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.entity.UserAiRoadmapState;
import com.compassed.compassed_api.domain.entity.UserProfile;
import com.compassed.compassed_api.domain.entity.UserRoadmapAssignment;
import com.compassed.compassed_api.local.QuestionBank;
import com.compassed.compassed_api.repository.FinalTestAttemptRepository;
import com.compassed.compassed_api.repository.NotificationRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.QuestionBankRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserProfileRepository;
import com.compassed.compassed_api.repository.UserProgressRepository;
import com.compassed.compassed_api.repository.UserAiRoadmapStateRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserRoadmapAssignmentRepository;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.AiService;
import com.compassed.compassed_api.service.LoginActivityService;
import com.compassed.compassed_api.service.PaymentService;
import com.compassed.compassed_api.service.RoleAccessService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@Profile("mysql")
@RequestMapping("/api/me")
public class UserPortalController {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProgressRepository userProgressRepository;
    private final PlacementResultRepository placementResultRepository;
    private final UserRoadmapAssignmentRepository assignmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationRepository notificationRepository;
    private final FinalTestAttemptRepository finalTestAttemptRepository;
    private final QuestionBankRepository questionBankRepository;
    private final UserAiRoadmapStateRepository userAiRoadmapStateRepository;
    private final RoleAccessService roleAccessService;
    private final LoginActivityService loginActivityService;
    private final PaymentService paymentService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserPortalController(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            UserProgressRepository userProgressRepository,
            PlacementResultRepository placementResultRepository,
            UserRoadmapAssignmentRepository assignmentRepository,
            SubscriptionRepository subscriptionRepository,
            SubjectRepository subjectRepository,
            NotificationRepository notificationRepository,
            FinalTestAttemptRepository finalTestAttemptRepository,
            QuestionBankRepository questionBankRepository,
            UserAiRoadmapStateRepository userAiRoadmapStateRepository,
            RoleAccessService roleAccessService,
            LoginActivityService loginActivityService,
            PaymentService paymentService,
            AiService aiService,
            ObjectMapper objectMapper) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userProgressRepository = userProgressRepository;
        this.placementResultRepository = placementResultRepository;
        this.assignmentRepository = assignmentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subjectRepository = subjectRepository;
        this.notificationRepository = notificationRepository;
        this.finalTestAttemptRepository = finalTestAttemptRepository;
        this.questionBankRepository = questionBankRepository;
        this.userAiRoadmapStateRepository = userAiRoadmapStateRepository;
        this.roleAccessService = roleAccessService;
        this.loginActivityService = loginActivityService;
        this.paymentService = paymentService;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/profile")
    public Map<String, Object> myProfile() {
        Long userId = currentUserService.requireCurrentUserId();
        User user = getUser(userId);
        UserProfile profile = getOrCreateProfile(user);
        return profilePayload(user, profile);
    }

    @PutMapping("/profile")
    public Map<String, Object> updateMyProfile(@RequestBody UpdateMyProfileRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        User user = getUser(userId);
        UserProfile profile = getOrCreateProfile(user);
        if (request != null) {
            if (request.getFullName() != null) {
                user.setFullName(request.getFullName().trim());
                userRepository.save(user);
            }
            if (request.getLearningGoal() != null) {
                profile.setLearningGoal(request.getLearningGoal());
            }
            if (request.getTargetScore() != null) {
                int target = Math.max(0, Math.min(100, request.getTargetScore()));
                profile.setTargetScore(target);
            }
            if (request.getAcademicTrack() != null) {
                profile.setAcademicTrack(normalizeAcademicTrack(request.getAcademicTrack()));
                profile.setAcademicTrackConfirmed(true);
            }
            if (request.getNotifyEmail() != null) {
                profile.setNotifyEmail(Boolean.TRUE.equals(request.getNotifyEmail()));
            }
            if (request.getNotifyInApp() != null) {
                profile.setNotifyInApp(Boolean.TRUE.equals(request.getNotifyInApp()));
            }
            profile.setUpdatedAt(LocalDateTime.now());
            userProfileRepository.save(profile);
        }
        return profilePayload(user, profile);
    }

    @PutMapping("/password")
    public Map<String, Object> changeMyPassword(@RequestBody ChangeMyPasswordRequest request) {
        Long userId = currentUserService.requireCurrentUserId();
        User user = getUser(userId);
        if (request == null || request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters");
        }
        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new RuntimeException("Current password is required");
            }
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Current password is incorrect");
            }
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return Map.of("updated", true);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> myDashboard() {
        Long userId = currentUserService.requireCurrentUserId();
        User user = getUser(userId);
        UserProfile profile = getOrCreateProfile(user);
        List<PlacementResult> myPlacements = placementResultRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        List<UserRoadmapAssignment> assignments = assignmentRepository.findByUser_Id(userId);

        long completedLessons = userProgressRepository.findAll().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .filter(p -> p.getLessonId() != null && p.getLessonId() > 0)
                .filter(p -> Boolean.TRUE.equals(p.getCompleted()))
                .count();
        long miniTestsDone = userProgressRepository.findAll().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .filter(p -> p.getLessonId() != null && p.getLessonId() > 0)
                .filter(p -> p.getScore() != null)
                .count();
        long finalTestsDone = userProgressRepository.findAll().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .filter(p -> p.getLessonId() != null && p.getLessonId() == 0L)
                .filter(p -> p.getScore() != null)
                .count();

        double avgScore = myPlacements.stream()
                .map(PlacementResult::getScorePercent)
                .filter(x -> x != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        int targetScore = profile.getTargetScore() == null ? 75 : profile.getTargetScore();
        int goalPercent = (int) Math.max(0, Math.min(100, (avgScore / Math.max(targetScore, 1)) * 100));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("profile", profilePayload(user, profile));
        out.put("overview", Map.of(
                "completedLessons", completedLessons,
                "miniTestsDone", miniTestsDone,
                "finalTestsDone", finalTestsDone,
                "averageScore", round1(avgScore),
                "targetScore", targetScore,
                "goalProgressPercent", goalPercent));
        out.put("roadmaps", assignments.stream().map(this::roadmapPayload).toList());
        out.put("testResults", myPlacements.stream().limit(20).map(this::placementPayload).toList());
        out.put("upcomingTests", buildUpcomingTests(assignments));
        out.put("ranking", rankingPayload(userId, avgScore));
        out.put("studyStreakDays", loginActivityService.computeStreak(userId));
        out.put("strengthWeakness", buildStrengthWeakness(myPlacements));
        out.put("recommendations", buildRecommendations(assignments, myPlacements));
        out.put("practiceQuestions", buildPracticeQuestions(assignments));
        out.put("notifications", notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .limit(10)
                .map(n -> Map.of(
                        "id", n.getId(),
                        "title", n.getTitle(),
                        "message", n.getMessage(),
                        "type", n.getType(),
                        "read", n.isReadFlag(),
                        "createdAt", String.valueOf(n.getCreatedAt())))
                .toList());
        out.put("notificationSettings", Map.of(
                "notifyEmail", profile.isNotifyEmail(),
                "notifyInApp", profile.isNotifyInApp()));
        out.put("resultArchive", Map.of(
                "downloadCsvUrl", "/api/me/tests/export",
                "totalPlacementRecords", myPlacements.size()));
        out.put("progressChart", buildProgressChart(myPlacements, assignments));
        return out;
    }

    @GetMapping("/subscriptions")
    public Map<String, Object> mySubscriptions() {
        Long userId = currentUserService.requireCurrentUserId();
        paymentService.ensureSubscriptionsProvisionedFromSuccessfulPayments(userId);
        var active = subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        var activeIds = active.stream().map(Subscription::getSubjectId).toList();
        var available = subjectRepository.findAll().stream()
                .filter(s -> !activeIds.contains(s.getId()))
                .map(s -> Map.of(
                        "subjectId", s.getId(),
                        "subjectCode", s.getCode(),
                        "subjectName", s.getName()))
                .toList();
        var activeRows = active.stream().map(s -> {
            Subject subject = subjectRepository.findById(s.getSubjectId()).orElse(null);
            if (subject == null) return null;
            
            var assigned = assignmentRepository.findByUserIdAndSubjectId(userId, s.getSubjectId()).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("subjectId", subject.getId());
            item.put("subjectCode", subject.getCode());
            item.put("subjectName", subject.getName());
            item.put("phase", assigned == null ? "NOT_STARTED" : assigned.getPhase());
            item.put("level", assigned == null ? "" : assigned.getRoadmap().getLevel().name());
            item.put("active", s.getIsActive());
            return item;
        }).filter(item -> item != null).toList();
        Map<String, Object> ranking = rankingPayload(userId, 0.0);
        int streak = loginActivityService.computeStreak(userId);
        return Map.of(
                "activeSubscriptions", activeRows,
                "availableSubjects", available,
                "rank", ranking.get("rank"),
                "totalLearners", ranking.get("totalLearners"),
                "studyStreakDays", streak);
    }

    @GetMapping("/subjects/{subjectId}/ai-roadmap")
    public AiGeneratedRoadmapResponse generateAiRoadmap(
            @PathVariable Long subjectId,
            @RequestParam(name = "action", required = false, defaultValue = "auto") String action) {
        Long userId = currentUserService.requireCurrentUserId();
        User user = getUser(userId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        UserProfile profile = getOrCreateProfile(user);
        String academicTrack = normalizeAcademicTrack(profile.getAcademicTrack());

        PlacementResult placement = placementResultRepository
                .findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subjectId)
                .orElseThrow(() -> new RuntimeException("Need placement result before generating AI roadmap"));

        com.compassed.compassed_api.domain.QuestionBank.Level level = com.compassed.compassed_api.domain.QuestionBank.Level
                .valueOf(placement.getLevel().name());

        List<com.compassed.compassed_api.domain.QuestionBank> qb = questionBankRepository
                .findBySubjectIdAndLevelAndGradeBandAndIsActiveTrue(subjectId, level, academicTrack);
        if (qb.isEmpty()) {
            qb = questionBankRepository.findBySubjectIdAndLevelAndIsActiveTrue(subjectId, level);
        }
        if (qb.isEmpty()) {
            throw new RuntimeException("Question bank is empty for this subject/level/track");
        }

        List<String> skills = qb.stream()
                .map(com.compassed.compassed_api.domain.QuestionBank::getSkillType)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();
        RoadmapFramework framework = resolveFramework(placement.getLevel().name(), academicTrack, subject.getCode());
        String skillsJson = buildRoadmapPromptInput(framework, skills);

        String actionNormalized = action == null ? "auto" : action.trim().toLowerCase();
        boolean subscribed = subscriptionRepository.existsByUserIdAndSubjectIdAndIsActiveTrue(userId, subjectId);
        int refreshLimit = subscribed ? 5 : 1;

        UserAiRoadmapState state = userAiRoadmapStateRepository
                .findByUser_IdAndSubject_Id(userId, subjectId)
                .orElse(null);
        boolean initialized = state != null && state.getRoadmapGuideJson() != null && !state.getRoadmapGuideJson().isBlank();
        int refreshCountUsed = state == null || state.getRefreshCount() == null ? 0 : state.getRefreshCount();

        String guide = initialized ? state.getRoadmapGuideJson() : null;
        if ("view".equals(actionNormalized)) {
            if (initialized && !isUsableRoadmapGuide(guide)) {
                guide = generateRoadmapGuideSafely(
                        subject.getCode(),
                        placement.getLevel().name(),
                        academicTrack,
                        placement.getScorePercent() == null ? 0.0 : placement.getScorePercent(),
                        skillsJson,
                        framework,
                        skills);
                state = saveRoadmapState(user, subject, state, guide, refreshCountUsed, false);
                initialized = true;
            }
        } else if ("initialize".equals(actionNormalized)) {
            if (!initialized) {
                guide = generateRoadmapGuideSafely(
                        subject.getCode(),
                        placement.getLevel().name(),
                        academicTrack,
                        placement.getScorePercent() == null ? 0.0 : placement.getScorePercent(),
                        skillsJson,
                        framework,
                        skills);
                state = saveRoadmapState(user, subject, state, guide, refreshCountUsed, true);
                initialized = true;
            }
        } else if ("refresh".equals(actionNormalized)) {
            if (!initialized) {
                throw new RuntimeException("ROADMAP_NOT_INITIALIZED: Please initialize roadmap first");
            }
            boolean currentGuideIsFallback = isFallbackGuide(guide);
            if (!currentGuideIsFallback && refreshCountUsed >= refreshLimit) {
                throw new RuntimeException("REFRESH_LIMIT_REACHED: You have used all free roadmap refreshes");
            }
            guide = generateRoadmapGuideSafely(
                    subject.getCode(),
                    placement.getLevel().name(),
                    academicTrack,
                    placement.getScorePercent() == null ? 0.0 : placement.getScorePercent(),
                    skillsJson,
                    framework,
                    skills);
            if (!currentGuideIsFallback) {
                refreshCountUsed += 1;
            }
            state = saveRoadmapState(user, subject, state, guide, refreshCountUsed, false);
            initialized = true;
        } else {
            // auto mode for backward compatibility
            if (!initialized) {
                guide = generateRoadmapGuideSafely(
                        subject.getCode(),
                        placement.getLevel().name(),
                        academicTrack,
                        placement.getScorePercent() == null ? 0.0 : placement.getScorePercent(),
                        skillsJson,
                        framework,
                        skills);
                state = saveRoadmapState(user, subject, state, guide, refreshCountUsed, true);
                initialized = true;
            }
        }

        if (state != null && state.getRefreshCount() != null) {
            refreshCountUsed = state.getRefreshCount();
        }
        int refreshRemaining = Math.max(0, refreshLimit - refreshCountUsed);
        boolean canRefresh = initialized && refreshRemaining > 0;

        List<AiGeneratedRoadmapResponse.RoadmapModuleItem> personalizedModules = normalizeRoadmapModules(
                extractRoadmapModulesFromGuide(guide),
                placement.getLevel().name(),
                framework.modules(),
                skills);
        List<String> moduleTitles = personalizedModules.stream()
                .map(AiGeneratedRoadmapResponse.RoadmapModuleItem::getTitle)
                .filter(t -> t != null && !t.isBlank())
                .toList();

        List<com.compassed.compassed_api.domain.QuestionBank> shuffled = new ArrayList<>(qb);
        Collections.shuffle(shuffled);
        List<SkillPlan> miniPlan = buildSkillPlan(skills, framework.preferredSkills(), 10);
        List<SkillPlan> finalPlan = buildSkillPlan(skills, framework.preferredSkills(), 20);
        List<com.compassed.compassed_api.domain.QuestionBank> miniRows = pickQuestionsByPlan(shuffled, miniPlan, 10);
        List<com.compassed.compassed_api.domain.QuestionBank> finalRows = pickQuestionsByPlan(shuffled, finalPlan, 20);

        AiGeneratedRoadmapResponse response = new AiGeneratedRoadmapResponse();
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setLevel(placement.getLevel().name());
        response.setAcademicTrack(academicTrack);
        response.setFrameworkCode(framework.code());
        response.setFrameworkTitle(framework.title());
        response.setFrameworkDescription(framework.description());
        response.setFrameworkModules(moduleTitles.isEmpty() ? framework.modules() : moduleTitles);
        response.setRoadmapModules(personalizedModules);
        response.setPlacementScorePercent(
                placement.getScorePercent() == null ? 0.0 : round1(placement.getScorePercent()));
        response.setRoadmapGuideJson(guide);
        response.setMiniTestPlan(toSkillPlanItems(miniPlan));
        response.setFinalTestPlan(toSkillPlanItems(finalPlan));
        response.setMiniTestDraft(toQuestionItems(miniRows));
        response.setFinalTestDraft(toQuestionItems(finalRows));
        response.setRoadmapInitialized(initialized);
        response.setRefreshCountUsed(refreshCountUsed);
        response.setRefreshCountLimit(refreshLimit);
        response.setRefreshCountRemaining(refreshRemaining);
        response.setCanRefresh(canRefresh);
        return response;
    }

    @GetMapping("/subjects/{subjectId}/ai-roadmap/lessons")
    public AiRoadmapLessonResponse generateAiRoadmapLesson(
            @PathVariable Long subjectId,
            @RequestParam Integer moduleNo,
            @RequestParam Integer lessonNo) {
        Long userId = currentUserService.requireCurrentUserId();
        User user = getUser(userId);
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        UserProfile profile = getOrCreateProfile(user);
        String academicTrack = normalizeAcademicTrack(profile.getAcademicTrack());

        PlacementResult placement = placementResultRepository
                .findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subjectId)
                .orElseThrow(() -> new RuntimeException("Need placement result before generating lesson"));

        UserAiRoadmapState state = userAiRoadmapStateRepository
                .findByUser_IdAndSubject_Id(userId, subjectId)
                .orElseThrow(() -> new RuntimeException("ROADMAP_NOT_INITIALIZED: Please initialize roadmap first"));
        if (state.getRoadmapGuideJson() == null || state.getRoadmapGuideJson().isBlank()) {
            throw new RuntimeException("ROADMAP_NOT_INITIALIZED: Please initialize roadmap first");
        }

        com.compassed.compassed_api.domain.QuestionBank.Level qbLevel = com.compassed.compassed_api.domain.QuestionBank.Level
                .valueOf(placement.getLevel().name());
        List<com.compassed.compassed_api.domain.QuestionBank> qb = questionBankRepository
                .findBySubjectIdAndLevelAndGradeBandAndIsActiveTrue(subjectId, qbLevel, academicTrack);
        if (qb.isEmpty()) {
            qb = questionBankRepository.findBySubjectIdAndLevelAndIsActiveTrue(subjectId, qbLevel);
        }
        List<String> skills = qb.stream()
                .map(com.compassed.compassed_api.domain.QuestionBank::getSkillType)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        RoadmapFramework framework = resolveFramework(placement.getLevel().name(), academicTrack, subject.getCode());
        List<AiGeneratedRoadmapResponse.RoadmapModuleItem> modules = normalizeRoadmapModules(
                extractRoadmapModulesFromGuide(state.getRoadmapGuideJson()),
                placement.getLevel().name(),
                framework.modules(),
                skills);
        AiGeneratedRoadmapResponse.RoadmapModuleItem module = modules.stream()
                .filter(x -> x != null && x.getModuleNo() != null && x.getModuleNo().equals(moduleNo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Module not found in AI roadmap"));
        AiGeneratedRoadmapResponse.LessonPlanItem lesson = Optional.ofNullable(module.getLessonPlan()).orElse(List.of())
                .stream()
                .filter(x -> x != null && x.getLessonNo() != null && x.getLessonNo().equals(lessonNo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lesson not found in AI roadmap module"));

        return buildFallbackLessonContent(subject, placement.getLevel().name(), academicTrack, module, lesson);
    }

    @GetMapping(value = "/tests/export", produces = "text/csv")
    public ResponseEntity<String> exportTestsCsv() {
        Long userId = currentUserService.requireCurrentUserId();
        List<PlacementResult> rows = placementResultRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        StringBuilder csv = new StringBuilder("submitted_at,subject_code,subject_name,level,score_percent\n");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (PlacementResult r : rows) {
            String submittedAt = r.getCreatedAt() == null ? "" : r.getCreatedAt().format(fmt);
            csv.append(submittedAt).append(",")
                    .append(safeCsv(r.getSubject().getCode())).append(",")
                    .append(safeCsv(r.getSubject().getName())).append(",")
                    .append(r.getLevel() == null ? "" : r.getLevel().name()).append(",")
                    .append(r.getScorePercent() == null ? "" : round1(r.getScorePercent()))
                    .append("\n");
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compassed-test-results.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    private UserAiRoadmapState saveRoadmapState(
            User user,
            Subject subject,
            UserAiRoadmapState existing,
            String guide,
            int refreshCount,
            boolean initializeAtIfNeeded) {
        UserAiRoadmapState state = existing == null ? new UserAiRoadmapState() : existing;
        state.setUser(user);
        state.setSubject(subject);
        state.setRoadmapGuideJson(guide);
        state.setRefreshCount(Math.max(0, refreshCount));
        LocalDateTime now = LocalDateTime.now();
        if (initializeAtIfNeeded && state.getInitializedAt() == null) {
            state.setInitializedAt(now);
        }
        state.setUpdatedAt(now);
        return userAiRoadmapStateRepository.save(state);
    }

    private String generateRoadmapGuideSafely(
            String subjectCode,
            String level,
            String academicTrack,
            double placementScorePercent,
            String availableSkillsJson,
            RoadmapFramework framework,
            List<String> skills) {
        try {
            String guide = aiService.generatePersonalizedRoadmapGuide(
                    subjectCode,
                    level,
                    academicTrack,
                    placementScorePercent,
                    availableSkillsJson);
            if (!isUsableRoadmapGuide(guide)) {
                return buildFallbackRoadmapGuideJson(level, framework, skills);
            }
            return extractJsonPayload(guide);
        } catch (Exception ex) {
            return buildFallbackRoadmapGuideJson(level, framework, skills);
        }
    }

    private String buildRoadmapPromptInput(RoadmapFramework framework, List<String> skills) {
        List<String> compactSkills = Optional.ofNullable(skills)
                .orElse(List.of())
                .stream()
                .map(skill -> skill == null ? "" : skill.trim())
                .filter(skill -> !skill.isBlank())
                .limit(12)
                .toList();
        Map<String, Object> compactFramework = Map.of(
                "code", framework.code(),
                "title", framework.title(),
                "modules", framework.modules());
        return toJsonQuietly(Map.of(
                "availableSkills", compactSkills,
                "skillCount", Optional.ofNullable(skills).orElse(List.of()).size(),
                "framework", compactFramework));
    }

    private String buildFallbackRoadmapGuideJson(String level, RoadmapFramework framework, List<String> skills) {
        List<Map<String, Object>> roadmapSteps = new ArrayList<>();
        List<String> moduleTitles = framework == null || framework.modules() == null || framework.modules().isEmpty()
                ? List.of("Module 1", "Module 2", "Module 3", "Module 4", "Module 5")
                : framework.modules();
        List<String> stageTemplates = List.of(
                "Khởi động nền tảng",
                "Ôn trọng tâm",
                "Luyện dạng cơ bản",
                "Luyện dạng nâng dần",
                "Ứng dụng theo chủ đề",
                "Phân tích lỗi thường gặp",
                "Bài tập tổng hợp",
                "Rèn tốc độ xử lý",
                "Củng cố điểm yếu",
                "Mini test cuối module");
        for (int i = 0; i < 5; i++) {
            String title = moduleTitles.get(i % moduleTitles.size());
            List<String> focus = pickFocusSkills(skills, i);
            List<Map<String, Object>> lessonPlan = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                String stage = stageTemplates.get(j % stageTemplates.size());
                String skill = focus.get(j % focus.size());
                lessonPlan.add(Map.of(
                        "lessonNo", j + 1,
                        "title", "Bài " + String.format("%02d", j + 1) + " - " + stage,
                        "summary", "Module " + (i + 1) + ": " + stage + " với trọng tâm " + skill + " theo mức " + level + ".",
                        "duration", "45 phút"));
            }
            roadmapSteps.add(Map.of(
                    "week", i + 1,
                    "title", title,
                    "focusSkills", focus,
                    "studyGuide", "Roadmap được tạo theo dữ liệu nội bộ do dịch vụ AI tạm thời không khả dụng.",
                    "targetScore", defaultTargetByLevel(level),
                    "duration", "2 tuần",
                    "lessonPlan", lessonPlan));
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("mode", "FALLBACK");
        payload.put("objective", "Củng cố kiến thức và nâng cấp theo level hiện tại.");
        payload.put("roadmapSteps", roadmapSteps);
        payload.put("miniTestBlueprint", Map.of("questionCount", 10));
        payload.put("finalTestBlueprint", Map.of("questionCount", 20));
        return toJsonQuietly(payload);
    }

    private AiRoadmapLessonResponse parseLessonContentOrFallback(
            String rawContent,
            Subject subject,
            String level,
            String academicTrack,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        try {
            JsonNode root = objectMapper.readTree(extractJsonPayload(rawContent));
            AiRoadmapLessonResponse response = new AiRoadmapLessonResponse();
            response.setSubjectId(subject.getId());
            response.setSubjectCode(subject.getCode());
            response.setSubjectName(subject.getName());
            response.setLevel(level);
            response.setAcademicTrack(academicTrack);
            response.setModuleNo(module.getModuleNo());
            response.setModuleTitle(module.getTitle());
            response.setLessonNo(lesson.getLessonNo());
            response.setLessonTitle(lesson.getTitle());
            response.setLessonSummary(lesson.getSummary());
            response.setDuration(lesson.getDuration());
            response.setLearningObjectives(readStringList(root.path("learningObjectives")));
            response.setLessonSections(readLessonSections(root.path("lessonSections")));
            response.setPracticeTasks(readStringList(root.path("practiceTasks")));
            response.setKeyTakeaways(readStringList(root.path("keyTakeaways")));
            response.setReflectionPrompt(root.path("reflectionPrompt").asText(""));
            response.setHomework(root.path("homework").asText(""));
            if (!response.getLessonSections().isEmpty()) {
                return response;
            }
        } catch (Exception ignored) {
        }
        return buildFallbackLessonContent(subject, level, academicTrack, module, lesson);
    }

    private AiRoadmapLessonResponse buildFallbackLessonContent(
            Subject subject,
            String level,
            String academicTrack,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        return buildStructuredFallbackLessonContent(subject, level, academicTrack, module, lesson);
    }

    private AiRoadmapLessonResponse buildStructuredFallbackLessonContent(
            Subject subject,
            String level,
            String academicTrack,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        AiRoadmapLessonResponse response = new AiRoadmapLessonResponse();
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        response.setLevel(level);
        response.setAcademicTrack(academicTrack);
        response.setModuleNo(module.getModuleNo());
        response.setModuleTitle(module.getTitle());
        response.setLessonNo(lesson.getLessonNo());
        response.setLessonTitle(lesson.getTitle());
        response.setLessonSummary(lesson.getSummary());
        response.setDuration(lesson.getDuration());
        if (System.currentTimeMillis() >= 0) {
        response.setLearningObjectives(fallbackLearningObjectives(level, module, lesson));
        List<AiRoadmapLessonResponse.LessonSectionItem> structuredSections = new ArrayList<>();
        structuredSections.add(section(
                "Ban se hoc gi trong bai nay",
                fallbackLessonIntro(subject.getName(), level, lesson.getTitle(), lesson.getSummary()),
                fallbackLessonIntroBullets(level, academicTrack)));
        structuredSections.add(section(
                "Khung kien thuc cot loi",
                fallbackCoreKnowledge(level, module, lesson),
                fallbackKnowledgeBullets(level, Optional.ofNullable(module.getFocusSkills()).orElse(List.of("Ky nang trong tam")))));
        structuredSections.add(section(
                "Cach trien khai bai hoc",
                fallbackLearningFlow(level, lesson),
                fallbackLearningFlowBullets(level)));
        structuredSections.add(section(
                "Vi du va huong van dung",
                fallbackExampleGuide(subject.getCode(), level, lesson.getTitle(), module.getFocusSkills()),
                fallbackExampleBullets(subject.getCode(), level)));
        response.setLessonSections(structuredSections);
        response.setPracticeTasks(fallbackPracticeTasks(subject.getCode(), level, module, lesson));
        response.setKeyTakeaways(fallbackKeyTakeaways(level, module, lesson));
        response.setReflectionPrompt("Sau bai nay, em co the tu giai thich lai kien thuc bang loi cua minh hay chua? Phan nao con mo ho nhat?");
        response.setHomework(fallbackHomework(subject.getCode(), level, lesson));
        return response;
        }
        response.setLearningObjectives(List.of(
                "Nắm đúng mục tiêu trọng tâm của bài học này.",
                "Hiểu cách áp dụng kiến thức vào dạng bài thuộc module hiện tại.",
                "Chuẩn bị nền để làm mini test của module."));

        List<AiRoadmapLessonResponse.LessonSectionItem> sections = new ArrayList<>();
        sections.add(section(
                "Mục tiêu bài học",
                lesson.getSummary() == null || lesson.getSummary().isBlank()
                        ? "Bài học này được AI roadmap xác định là mắt xích cần thiết trong lộ trình hiện tại của bạn."
                        : lesson.getSummary(),
                List.of(
                        "Môn: " + subject.getName(),
                        "Level: " + level,
                        "Lộ trình: " + academicTrack)));
        sections.add(section(
                "Kiến thức trọng tâm",
                module.getStudyGuide() == null || module.getStudyGuide().isBlank()
                        ? "Tập trung bám sát kiến thức lõi của module và luyện đúng nhóm kỹ năng ưu tiên."
                        : module.getStudyGuide(),
                Optional.ofNullable(module.getFocusSkills()).orElse(List.of("Kỹ năng trọng tâm"))));
        sections.add(section(
                "Cách học bài này",
                "Hãy đọc phần lý thuyết, tự tóm tắt lại bằng ngôn ngữ của mình, sau đó giải 2-3 ví dụ ngắn trước khi chuyển sang bài tiếp theo.",
                List.of(
                        "Tóm tắt ý chính sau mỗi phần",
                        "Đối chiếu với lỗi thường gặp của bạn",
                        "Hoàn thành bài trong đúng thời lượng đề xuất")));
        response.setLessonSections(sections);
        response.setPracticeTasks(List.of(
                "Viết lại 3 ý chính quan trọng nhất của bài học.",
                "Làm 2 ví dụ tự luyện theo đúng kỹ năng trọng tâm của module.",
                "Tự đánh giá phần nào còn chưa chắc để hỏi lại AI Tutor."));
        response.setKeyTakeaways(List.of(
                "Hiểu đúng mục tiêu của bài hiện tại trong toàn bộ roadmap.",
                "Biết mình đang cần luyện kỹ năng nào trước.",
                "Sẵn sàng mở bài tiếp theo sau khi hoàn thành phần tự luyện."));
        response.setReflectionPrompt("Sau bài này, phần nào bạn thấy còn mơ hồ nhất và cần ôn lại ngay?");
        response.setHomework("Hoàn thành ghi chú cá nhân và làm ít nhất 2 bài luyện tập ngắn bám đúng kỹ năng trọng tâm.");
        return response;
    }

    private AiRoadmapLessonResponse.LessonSectionItem section(String heading, String body, List<String> bullets) {
        AiRoadmapLessonResponse.LessonSectionItem item = new AiRoadmapLessonResponse.LessonSectionItem();
        item.setHeading(heading);
        item.setBody(body);
        item.setBullets(bullets);
        return item;
    }

    private List<String> readStringList(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        node.forEach(item -> {
            String value = item.asText("");
            if (!value.isBlank()) {
                out.add(value);
            }
        });
        return out;
    }

    private List<AiRoadmapLessonResponse.LessonSectionItem> readLessonSections(JsonNode node) {
        if (node == null || !node.isArray() || node.isEmpty()) {
            return List.of();
        }
        List<AiRoadmapLessonResponse.LessonSectionItem> out = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            AiRoadmapLessonResponse.LessonSectionItem section = new AiRoadmapLessonResponse.LessonSectionItem();
            section.setHeading(item.path("heading").asText(""));
            section.setBody(item.path("body").asText(""));
            section.setBullets(readStringList(item.path("bullets")));
            if ((section.getHeading() != null && !section.getHeading().isBlank())
                    || (section.getBody() != null && !section.getBody().isBlank())
                    || !section.getBullets().isEmpty()) {
                out.add(section);
            }
        }
        return out;
    }

    private boolean isUsableRoadmapGuide(String guideJson) {
        if (guideJson == null || guideJson.isBlank()) return false;
        try {
            JsonNode root = objectMapper.readTree(extractJsonPayload(guideJson));
            JsonNode steps = root.path("roadmapSteps");
            return steps.isArray() && !steps.isEmpty();
        } catch (Exception ex) {
            return false;
        }
    }

    private String extractJsonPayload(String raw) {
        if (raw == null) return "";
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        int objStart = text.indexOf('{');
        int objEnd = text.lastIndexOf('}');
        if (objStart >= 0 && objEnd > objStart) {
            return text.substring(objStart, objEnd + 1);
        }
        return text;
    }

    private String fallbackModuleStudyGuide(String level, int moduleNo, String title, List<String> focus) {
        String focusText = (focus == null || focus.isEmpty()) ? "ky nang trong tam" : String.join(", ", focus);
        return switch (level == null ? "L1" : level) {
            case "L3" -> "Module " + moduleNo + " phat trien chuyen sau tu chu de " + title
                    + ", uu tien xu ly bai kho, toi uu toc do va do chinh xac cho cac nhom " + focusText + ".";
            case "L2" -> "Module " + moduleNo + " mo rong va ket noi cac dang bai cua " + title
                    + ", giup hoc sinh luyen chac phan " + focusText + " truoc khi vao checkpoint.";
            default -> "Module " + moduleNo + " xay nen cho " + title
                    + ", tap trung nam khai niem dung, lam duoc vi du mau va tranh sai sot co ban o cac nhom " + focusText + ".";
        };
    }

    private List<String> fallbackLearningObjectives(String level,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        return switch (level == null ? "L1" : level) {
            case "L3" -> List.of(
                    "Hieu ban chat cua dang bai " + safeText(lesson.getTitle(), "nang cao") + ".",
                    "Biet chon chien luoc giai nhanh nhung van kiem soat loi.",
                    "San sang ap dung vao cau van dung hoac cau phan hoa.");
            case "L2" -> List.of(
                    "Nam chac quy trinh lam bai " + safeText(lesson.getTitle(), "trong tam") + ".",
                    "Biet phan biet cac bien the thuong gap de chon huong giai phu hop.",
                    "Tang do on dinh truoc khi sang bai luyen kho hon.");
            default -> List.of(
                    "Hieu dung khai niem va muc tieu cua bai " + safeText(lesson.getTitle(), "nen tang") + ".",
                    "Lam duoc vi du mau theo tung buoc ro rang.",
                    "Tao nen de hoc tiep cac bai sau trong module.");
        };
    }

    private String fallbackLessonIntro(String subjectName, String level, String lessonTitle, String lessonSummary) {
        String title = safeText(lessonTitle, "bai hoc hien tai");
        String summary = safeText(lessonSummary, "");
        String base = "Bai nay thuoc mon " + subjectName + " o muc " + level + ". Trong tam la " + title + ".";
        if (!summary.isBlank()) {
            base += " Muc tieu gan nhat la: " + summary;
        }
        return base;
    }

    private List<String> fallbackLessonIntroBullets(String level, String academicTrack) {
        return List.of(
                "Level hien tai: " + safeText(level, "L1"),
                "Lo trinh hoc: " + safeText(academicTrack, "GRADE_11"),
                "Hoc theo dung nhip do cua roadmap ca nhan");
    }

    private String fallbackCoreKnowledge(String level,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        String focusText = String.join(", ", Optional.ofNullable(module.getFocusSkills()).orElse(List.of("ky nang trong tam")));
        return switch (level == null ? "L1" : level) {
            case "L3" -> "Phan nay can di tu ban chat den chien luoc xu ly. Em nen xac dinh dau hieu nhan dien dang bai, cong thuc hoac cau truc then chot, sau do luyen cach bien doi ngan gon. Trong tam bai nay xoay quanh: " + focusText + ".";
            case "L2" -> "Phan nay can hieu quy trinh lam bai mot cach co he thong: nhan dien dang, chon huong lam, kiem tra dieu kien va rut kinh nghiem tu loi sai. Trong tam bai nay xoay quanh: " + focusText + ".";
            default -> "Phan nay can nam chac khai niem co ban, ky hieu quan trong va vi du mau. Muc tieu la hieu dung truoc, lam dung sau, chua can tang toc qua som. Trong tam bai nay xoay quanh: " + focusText + ".";
        };
    }

    private List<String> fallbackKnowledgeBullets(String level, List<String> focusSkills) {
        List<String> out = new ArrayList<>(focusSkills);
        if ("L3".equals(level)) out.add("Uu tien cau van dung va toi uu toc do");
        else if ("L2".equals(level)) out.add("Tap trung lam chac cac bien the thuong gap");
        else out.add("Uu tien hieu dung khai niem cot loi");
        return out;
    }

    private String fallbackLearningFlow(String level, AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        return switch (level == null ? "L1" : level) {
            case "L3" -> "Hay doc nhanh phan ly thuyet de xac dinh cong cu chinh, sau do chuyen ngay sang vi du dien hinh. Moi vi du can tu giai thich vi sao chon cach lam do, roi moi luyen cau bien the tuong tu.";
            case "L2" -> "Hay hoc theo 3 buoc: doc vi du mau, tu lam lai khong nhin loi giai, roi luyen them mot bien the gan giong. Sau moi lan lam, so sanh loi sai de tranh lap lai.";
            default -> "Hay hoc cham va chac: doc khai niem, xem vi du mau, ghi lai quy tac bang loi cua minh, roi lam 1-2 bai ap dung co ban truoc khi chuyen sang phan tiep theo.";
        };
    }

    private List<String> fallbackLearningFlowBullets(String level) {
        return switch (level == null ? "L1" : level) {
            case "L3" -> List.of("Nhan dien dang bai", "Chon chien luoc toi uu", "Tu kiem loi sau moi vi du");
            case "L2" -> List.of("Lam lai vi du mau", "So sanh cac bien the", "Ghi lai loi sai thuong gap");
            default -> List.of("Nam dinh nghia/ky hieu", "Lam vi du tung buoc", "Tu tom tat quy tac chinh");
        };
    }

    private String fallbackExampleGuide(String subjectCode, String level, String lessonTitle, List<String> focusSkills) {
        String subject = safeText(subjectCode, "");
        String focus = String.join(", ", focusSkills == null ? List.of() : focusSkills);
        if ("ENGLISH".equalsIgnoreCase(subject) || "E".equalsIgnoreCase(subject)) {
            return "Voi bai " + safeText(lessonTitle, "nay") + ", hay tu tao 3 cau vi du chua diem ngu phap/tu vung trong tam roi doi chieu xem minh da dung dung cau truc chua. Uu tien cac nhom: " + focus + ".";
        }
        if ("LITERATURE".equalsIgnoreCase(subject) || "L".equalsIgnoreCase(subject)) {
            return "Voi bai " + safeText(lessonTitle, "nay") + ", hay tu viet dan y ngan hoac neu 2-3 luan diem chinh, sau do doi chieu xem cac dan chung da bam dung yeu cau chua. Uu tien cac nhom: " + focus + ".";
        }
        return "Voi bai " + safeText(lessonTitle, "nay") + ", hay lam 1 vi du mau day du tung buoc roi thu bien doi du kien de tao them 1 bai tuong tu. Uu tien cac nhom: " + focus + ".";
    }

    private List<String> fallbackExampleBullets(String subjectCode, String level) {
        if ("ENGLISH".equalsIgnoreCase(subjectCode) || "E".equalsIgnoreCase(subjectCode)) {
            return List.of("Tu tao cau vi du", "Kiem tra cau truc va loi ngu phap", "Doc lai de cung co phan xa");
        }
        if ("LITERATURE".equalsIgnoreCase(subjectCode) || "L".equalsIgnoreCase(subjectCode)) {
            return List.of("Tom tat y chinh", "Chon dan chung phu hop", "Viet lai bang dien dat cua minh");
        }
        return List.of("Lam vi du mau tung buoc", "Doi chieu dap an", "Tao them bai bien the de tu luyen");
    }

    private List<String> fallbackPracticeTasks(String subjectCode, String level,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        if ("ENGLISH".equalsIgnoreCase(subjectCode) || "E".equalsIgnoreCase(subjectCode)) {
            return List.of(
                    "Viet 5 cau hoac 1 doan ngan ap dung dung diem ngon ngu cua bai.",
                    "Tu sua lai cac cau chua chuan va ghi ro loi sai.",
                    "Doc lai to thanh tieng de tang ghi nho.");
        }
        if ("LITERATURE".equalsIgnoreCase(subjectCode) || "L".equalsIgnoreCase(subjectCode)) {
            return List.of(
                    "Viet tom tat 3 y chinh cua bai bang loi cua em.",
                    "Lap dan y ngan cho 1 cau hoi gan voi trong tam bai hoc.",
                    "Bo sung 2 dan chung hoac chi tiet tieu bieu.");
        }
        return List.of(
                "Lam 2 bai ap dung truc tiep theo dung mau vua hoc.",
                "Tu giai thich lai vi sao chon cach lam do o moi buoc.",
                "Ghi lai 1 loi sai de gap va cach tranh.");
    }

    private List<String> fallbackKeyTakeaways(String level,
            AiGeneratedRoadmapResponse.RoadmapModuleItem module,
            AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        return List.of(
                "Bai nay la mot mat xich trong module " + safeText(module.getTitle(), "hien tai") + ".",
                "Trong tam can nho la " + safeText(lesson.getTitle(), "kien thuc cua bai hoc") + ".",
                "Hoan thanh tot bai nay se giup em hoc nhanh hon o bai tiep theo.");
    }

    private String fallbackHomework(String subjectCode, String level, AiGeneratedRoadmapResponse.LessonPlanItem lesson) {
        if ("ENGLISH".equalsIgnoreCase(subjectCode) || "E".equalsIgnoreCase(subjectCode)) {
            return "Viet them 8-10 cau ap dung noi dung cua bai " + safeText(lesson.getTitle(), "nay") + " va tu ra loi ngu phap/tu vung.";
        }
        if ("LITERATURE".equalsIgnoreCase(subjectCode) || "L".equalsIgnoreCase(subjectCode)) {
            return "Hoan thien 1 doan van ngan hoac 1 dan y hoan chinh dua tren trong tam cua bai " + safeText(lesson.getTitle(), "nay") + ".";
        }
        return "Lam them 3 bai tu luyen ngan cho bai " + safeText(lesson.getTitle(), "nay") + " va ghi lai buoc giai chuan.";
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private boolean isFallbackGuide(String guideJson) {
        if (guideJson == null || guideJson.isBlank()) return false;
        try {
            JsonNode root = objectMapper.readTree(guideJson);
            return "FALLBACK".equalsIgnoreCase(root.path("mode").asText(""));
        } catch (Exception ex) {
            return false;
        }
    }

    private Map<String, Object> buildProgressChart(List<PlacementResult> placements,
            List<UserRoadmapAssignment> assignments) {
        int roadmapInProgress = (int) assignments.stream().filter(a -> !"COURSE_COMPLETED".equals(a.getPhase()))
                .count();
        int roadmapDone = (int) assignments.stream().filter(a -> "COURSE_COMPLETED".equals(a.getPhase())).count();
        int placementCount = placements.size();
        return Map.of(
                "labels", List.of("Placements", "Roadmap Active", "Roadmap Done"),
                "values", List.of(placementCount, roadmapInProgress, roadmapDone));
    }

    private List<Map<String, Object>> buildUpcomingTests(List<UserRoadmapAssignment> assignments) {
        List<Map<String, Object>> upcoming = new ArrayList<>();
        for (UserRoadmapAssignment a : assignments) {
            if ("FINAL_TEST".equals(a.getPhase())) {
                upcoming.add(Map.of(
                        "type", "FINAL_TEST",
                        "subject", a.getSubject().getName(),
                        "dueAt", String.valueOf(LocalDateTime.now().plusDays(2))));
            } else if ("MINI_TESTS".equals(a.getPhase())) {
                upcoming.add(Map.of(
                        "type", "MINI_TEST",
                        "subject", a.getSubject().getName(),
                        "dueAt", String.valueOf(LocalDateTime.now().plusDays(1))));
            }
        }
        return upcoming;
    }

    private Map<String, Object> rankingPayload(Long userId, double ignoredMyAvg) {
        Map<Long, List<Double>> scores = new LinkedHashMap<>();
        for (var r : finalTestAttemptRepository.findAll()) {
            if (r == null || r.getScore() == null || r.getUserId() == null) {
                continue;
            }
            scores.computeIfAbsent(r.getUserId(), k -> new ArrayList<>()).add(r.getScore().doubleValue());
        }
        List<Map.Entry<Long, Double>> ranking = scores.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().stream().mapToDouble(x -> x).average().orElse(0.0)))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .toList();
        Double myAvg = ranking.stream()
                .filter(x -> x.getKey().equals(userId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        int rank = 1;
        for (Map.Entry<Long, Double> row : ranking) {
            if (row.getKey().equals(userId)) {
                break;
            }
            rank++;
        }
        return Map.of(
                "myAverageScore", round1(myAvg == null ? 0.0 : myAvg),
                "rank", myAvg == null ? 0 : rank,
                "totalLearners", Math.max(1, ranking.size()));
    }

    private Map<String, Object> buildStrengthWeakness(List<PlacementResult> placements) {
        if (placements.isEmpty() || placements.get(0).getSkillAnalysisJson() == null) {
            return Map.of(
                    "strongTopics", List.of("Core skills"),
                    "weakTopics", List.of("Need more placement data"));
        }
        try {
            JsonNode root = objectMapper.readTree(placements.get(0).getSkillAnalysisJson());
            List<String> weakTopics = new ArrayList<>();
            JsonNode weakNode = root.path("weak_topics");
            if (weakNode.isArray()) {
                weakNode.forEach(n -> weakTopics.add(n.asText()));
            }
            List<Map<String, Object>> skills = new ArrayList<>();
            JsonNode skillsNode = root.path("skills");
            if (skillsNode.isArray()) {
                skillsNode.forEach(n -> skills.add(Map.of(
                        "name", n.path("name").asText("topic"),
                        "score", n.path("score").asInt(0))));
            }
            skills.sort(Comparator.comparingInt(x -> -((Integer) x.get("score"))));
            List<String> strong = skills.stream().limit(3).map(x -> String.valueOf(x.get("name"))).toList();
            return Map.of(
                    "strongTopics", strong.isEmpty() ? List.of("Developing") : strong,
                    "weakTopics", weakTopics.isEmpty() ? List.of("No weak topic detected") : weakTopics);
        } catch (Exception ex) {
            return Map.of(
                    "strongTopics", List.of("Developing"),
                    "weakTopics", List.of("Cannot parse analysis data"));
        }
    }

    private List<String> buildRecommendations(List<UserRoadmapAssignment> assignments,
            List<PlacementResult> placements) {
        List<String> items = new ArrayList<>();
        if (placements.isEmpty()) {
            items.add("Take your first placement test to unlock personalized recommendations.");
        }
        boolean hasFinal = assignments.stream().anyMatch(a -> "FINAL_TEST".equals(a.getPhase()));
        boolean hasMini = assignments.stream().anyMatch(a -> "MINI_TESTS".equals(a.getPhase()));
        if (hasFinal) {
            items.add("Complete final test in active roadmap to move to higher level.");
        }
        if (hasMini) {
            items.add("Finish pending mini-tests before final test.");
        }
        if (items.isEmpty()) {
            items.add("Continue daily lessons to maintain learning streak.");
        }
        return items;
    }

    private List<Map<String, Object>> buildPracticeQuestions(List<UserRoadmapAssignment> assignments) {
        if (assignments.isEmpty()) {
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("question", "Take placement test to get personalized practice.");
            return List.of(one);
        }
        UserRoadmapAssignment a = assignments.get(0);
        String subjectCode = a.getSubject().getCode();
        String level = a.getRoadmap().getLevel().name();
        List<Map<String, Object>> questions = QuestionBank.getQuestions(subjectCode, level);
        return questions.stream().limit(3).map(q -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("question", String.valueOf(q.get("question")));
            item.put("subject", subjectCode);
            item.put("level", level);
            return item;
        }).toList();
    }

    private Map<String, Object> placementPayload(PlacementResult r) {
        return Map.of(
                "subjectCode", r.getSubject().getCode(),
                "subjectName", r.getSubject().getName(),
                "scorePercent", round1(r.getScorePercent() == null ? 0.0 : r.getScorePercent()),
                "level", r.getLevel() == null ? "" : r.getLevel().name(),
                "submittedAt", String.valueOf(r.getCreatedAt()));
    }

    private Map<String, Object> roadmapPayload(UserRoadmapAssignment a) {
        return Map.of(
                "subjectId", a.getSubject().getId(),
                "subjectCode", a.getSubject().getCode(),
                "subjectName", a.getSubject().getName(),
                "level", a.getRoadmap().getLevel().name(),
                "phase", a.getPhase(),
                "replanCount", a.getReplanCount(),
                "assignedAt", String.valueOf(a.getAssignedAt()));
    }

    private String safeCsv(String value) {
        String v = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }

    private double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }

    private List<AiGeneratedRoadmapResponse.QuestionItem> toQuestionItems(
            List<com.compassed.compassed_api.domain.QuestionBank> rows) {
        return rows.stream().map(q -> {
            AiGeneratedRoadmapResponse.QuestionItem item = new AiGeneratedRoadmapResponse.QuestionItem();
            item.setQuestionId(q.getId());
            item.setSkillType(q.getSkillType());
            item.setQuestionText(q.getQuestionText());
            item.setOptions(q.getOptions());
            item.setCorrectAnswer(q.getCorrectAnswer());
            item.setExplanation(q.getExplanation());
            item.setDifficulty(q.getDifficulty());
            return item;
        }).toList();
    }

    private List<AiGeneratedRoadmapResponse.SkillPlanItem> toSkillPlanItems(List<SkillPlan> plans) {
        List<AiGeneratedRoadmapResponse.SkillPlanItem> out = new ArrayList<>();
        for (SkillPlan p : plans) {
            AiGeneratedRoadmapResponse.SkillPlanItem item = new AiGeneratedRoadmapResponse.SkillPlanItem();
            item.setSkillType(p.skillType());
            item.setCount(p.count());
            out.add(item);
        }
        return out;
    }

    private List<SkillPlan> buildSkillPlan(List<String> availableSkills, List<String> preferredKeywords, int total) {
        if (availableSkills == null || availableSkills.isEmpty() || total <= 0) {
            return List.of();
        }
        List<String> sorted = new ArrayList<>(availableSkills);
        sorted.sort((a, b) -> Integer.compare(scoreSkill(b, preferredKeywords), scoreSkill(a, preferredKeywords)));
        int bucket = Math.min(4, sorted.size());
        List<String> picked = sorted.subList(0, bucket);
        int base = total / bucket;
        int rem = total % bucket;
        List<SkillPlan> out = new ArrayList<>();
        for (int i = 0; i < picked.size(); i++) {
            int c = base + (i < rem ? 1 : 0);
            out.add(new SkillPlan(picked.get(i), c));
        }
        return out;
    }

    private List<com.compassed.compassed_api.domain.QuestionBank> pickQuestionsByPlan(
            List<com.compassed.compassed_api.domain.QuestionBank> pool,
            List<SkillPlan> plans,
            int total) {
        if (pool == null || pool.isEmpty() || total <= 0) {
            return List.of();
        }
        Map<String, List<com.compassed.compassed_api.domain.QuestionBank>> bySkill = new HashMap<>();
        for (com.compassed.compassed_api.domain.QuestionBank q : pool) {
            String key = normalizeSkillKey(q.getSkillType());
            bySkill.computeIfAbsent(key, k -> new ArrayList<>()).add(q);
        }
        bySkill.values().forEach(Collections::shuffle);
        List<com.compassed.compassed_api.domain.QuestionBank> out = new ArrayList<>();
        Set<Long> used = new LinkedHashSet<>();

        for (SkillPlan p : plans) {
            List<com.compassed.compassed_api.domain.QuestionBank> rows = bySkill.get(normalizeSkillKey(p.skillType()));
            if (rows == null || rows.isEmpty()) continue;
            for (com.compassed.compassed_api.domain.QuestionBank q : rows) {
                if (out.size() >= total) break;
                if (used.contains(q.getId())) continue;
                if (countSkill(out, p.skillType()) >= p.count()) break;
                out.add(q);
                used.add(q.getId());
            }
        }
        if (out.size() < total) {
            List<com.compassed.compassed_api.domain.QuestionBank> remain = new ArrayList<>(pool);
            Collections.shuffle(remain);
            for (com.compassed.compassed_api.domain.QuestionBank q : remain) {
                if (out.size() >= total) break;
                if (used.contains(q.getId())) continue;
                out.add(q);
                used.add(q.getId());
            }
        }
        return out;
    }

    private int countSkill(List<com.compassed.compassed_api.domain.QuestionBank> rows, String skillType) {
        String key = normalizeSkillKey(skillType);
        int c = 0;
        for (com.compassed.compassed_api.domain.QuestionBank q : rows) {
            if (normalizeSkillKey(q.getSkillType()).equals(key)) c++;
        }
        return c;
    }

    private int scoreSkill(String skill, List<String> preferredKeywords) {
        String s = normalizeSkillKey(skill);
        int score = 0;
        for (String k : preferredKeywords) {
            String key = normalizeSkillKey(k);
            if (s.contains(key) || key.contains(s)) score += 2;
        }
        return score;
    }

    private String normalizeSkillKey(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase();
    }

    private RoadmapFramework resolveFramework(String level, String track, String subjectCode) {
        String normLevel = level == null ? "L1" : level.trim().toUpperCase();
        String normTrack = normalizeAcademicTrack(track);
        String trackTitle = switch (normTrack) {
            case "GRADE_12" -> "Lớp 12";
            case "UNI_PREP" -> "Ôn thi đại học";
            default -> "Lớp 11";
        };
        if ("L1".equals(normLevel)) {
            return new RoadmapFramework(
                    "FW_L1_" + normTrack,
                    "Khung L1 - Củng cố nền tảng (" + trackTitle + ")",
                    "Tập trung lấp lỗ hổng kiến thức cơ bản, xây nền chắc trước khi tăng tốc.",
                    List.of("Module 1: Nền tảng cốt lõi", "Module 2: Củng cố kỹ năng cơ bản",
                            "Module 3: Luyện bài tập chuẩn", "Module 4: Ứng dụng mức cơ bản", "Module 5: Tổng ôn nền tảng"),
                    List.of(subjectCode, "cơ bản", "nền tảng", "khái niệm"));
        }
        if ("L2".equals(normLevel)) {
            return new RoadmapFramework(
                    "FW_L2_" + normTrack,
                    "Khung L2 - Tăng cường kiến thức (" + trackTitle + ")",
                    "Đẩy mạnh kỹ năng vận dụng, tăng tốc xử lý dạng bài trọng tâm.",
                    List.of("Module 1: Ôn lõi kiến thức", "Module 2: Mở rộng dạng bài",
                            "Module 3: Luyện tư duy vận dụng", "Module 4: Củng cố điểm yếu", "Module 5: Tổng ôn tăng cường"),
                    List.of(subjectCode, "vận dụng", "trọng tâm", "kỹ năng"));
        }
        return new RoadmapFramework(
                "FW_L3_" + normTrack,
                "Khung L3 - Ôn tập chuyên sâu (" + trackTitle + ")",
                "Tập trung chuyên đề khó, chiến lược làm bài và tối ưu điểm số.",
                List.of("Module 1: Chuyên đề nâng cao", "Module 2: Chiến lược giải nhanh",
                        "Module 3: Luyện đề chuyên sâu", "Module 4: Tối ưu điểm yếu cuối", "Module 5: Tổng duyệt mục tiêu điểm cao"),
                List.of(subjectCode, "nâng cao", "chuyên sâu", "chiến lược"));
    }

    private String toJsonQuietly(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private List<AiGeneratedRoadmapResponse.RoadmapModuleItem> extractRoadmapModulesFromGuide(String guideJson) {
        if (guideJson == null || guideJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(guideJson);
            JsonNode steps = root.path("roadmapSteps");
            if (!steps.isArray() || steps.isEmpty()) {
                return List.of();
            }
            List<AiGeneratedRoadmapResponse.RoadmapModuleItem> out = new ArrayList<>();
            for (int i = 0; i < steps.size(); i++) {
                JsonNode s = steps.get(i);
                AiGeneratedRoadmapResponse.RoadmapModuleItem item = new AiGeneratedRoadmapResponse.RoadmapModuleItem();
                item.setModuleNo(i + 1);
                item.setTitle(s.path("title").asText("Module " + (i + 1)));
                item.setStudyGuide(s.path("studyGuide").asText(""));
                item.setTargetScore(s.has("targetScore") && s.path("targetScore").canConvertToInt()
                        ? s.path("targetScore").asInt()
                        : null);
                item.setDuration(s.path("duration").asText(""));
                item.setLessonPlan(extractLessonPlan(s.path("lessonPlan")));
                List<String> focusSkills = new ArrayList<>();
                JsonNode focusNode = s.path("focusSkills");
                if (focusNode.isArray()) {
                    focusNode.forEach(x -> {
                        String v = x.asText("");
                        if (!v.isBlank()) focusSkills.add(v);
                    });
                }
                item.setFocusSkills(focusSkills);
                out.add(item);
            }
            return out;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<AiGeneratedRoadmapResponse.LessonPlanItem> extractLessonPlan(JsonNode lessonPlanNode) {
        if (lessonPlanNode == null || !lessonPlanNode.isArray() || lessonPlanNode.isEmpty()) {
            return List.of();
        }
        List<AiGeneratedRoadmapResponse.LessonPlanItem> lessons = new ArrayList<>();
        for (int i = 0; i < lessonPlanNode.size(); i++) {
            JsonNode n = lessonPlanNode.get(i);
            AiGeneratedRoadmapResponse.LessonPlanItem item = new AiGeneratedRoadmapResponse.LessonPlanItem();
            item.setLessonNo(i + 1);
            item.setTitle(n.path("title").asText("Bài " + (i + 1)));
            item.setSummary(n.path("summary").asText(""));
            item.setDuration(n.path("duration").asText("45 phút"));
            lessons.add(item);
        }
        return lessons;
    }

    private List<AiGeneratedRoadmapResponse.RoadmapModuleItem> normalizeRoadmapModules(
            List<AiGeneratedRoadmapResponse.RoadmapModuleItem> raw,
            String level,
            List<String> frameworkModules,
            List<String> skills) {
        List<AiGeneratedRoadmapResponse.RoadmapModuleItem> out = new ArrayList<>();
        if (raw != null) {
            for (AiGeneratedRoadmapResponse.RoadmapModuleItem item : raw) {
                if (item == null) continue;
                AiGeneratedRoadmapResponse.RoadmapModuleItem copy = new AiGeneratedRoadmapResponse.RoadmapModuleItem();
                copy.setTitle(item.getTitle());
                copy.setStudyGuide(item.getStudyGuide());
                copy.setDuration((item.getDuration() == null || item.getDuration().isBlank()) ? "2 tuần" : item.getDuration());
                copy.setTargetScore(item.getTargetScore() == null ? defaultTargetByLevel(level) : item.getTargetScore());
                copy.setFocusSkills((item.getFocusSkills() == null || item.getFocusSkills().isEmpty())
                        ? pickFocusSkills(skills, out.size()) : item.getFocusSkills());
                copy.setLessonPlan(ensureMinLessons(item.getLessonPlan(), copy.getTitle(), level, copy.getFocusSkills()));
                out.add(copy);
                if (out.size() >= 5) break;
            }
        }

        List<String> fallback = (frameworkModules == null || frameworkModules.isEmpty())
                ? List.of("Nền tảng cốt lõi", "Luyện dạng bài", "Kỹ năng vận dụng", "Củng cố điểm yếu", "Tổng ôn nâng cao")
                : frameworkModules;
        while (out.size() < 5) {
            int idx = out.size();
            AiGeneratedRoadmapResponse.RoadmapModuleItem m = new AiGeneratedRoadmapResponse.RoadmapModuleItem();
            m.setTitle(fallback.get(idx % fallback.size()));
            m.setStudyGuide("Giáo trình theo " + level + ", phù hợp năng lực đã phân loại.");
            m.setDuration("2 tuần");
            m.setTargetScore(defaultTargetByLevel(level));
            m.setFocusSkills(pickFocusSkills(skills, idx));
            m.setLessonPlan(ensureMinLessons(List.of(), m.getTitle(), level, m.getFocusSkills()));
            out.add(m);
        }

        for (int i = 0; i < out.size(); i++) {
            AiGeneratedRoadmapResponse.RoadmapModuleItem m = out.get(i);
            m.setModuleNo(i + 1);
            if (m.getLessonPlan() == null || m.getLessonPlan().isEmpty()) {
                m.setLessonPlan(ensureMinLessons(List.of(), m.getTitle(), level, m.getFocusSkills()));
            }
        }
        return out;
    }

    private List<AiGeneratedRoadmapResponse.LessonPlanItem> ensureMinLessons(
            List<AiGeneratedRoadmapResponse.LessonPlanItem> raw,
            String moduleTitle,
            String level,
            List<String> focusSkills) {
        List<AiGeneratedRoadmapResponse.LessonPlanItem> out = new ArrayList<>();
        if (raw != null) {
            for (AiGeneratedRoadmapResponse.LessonPlanItem item : raw) {
                if (item == null) continue;
                AiGeneratedRoadmapResponse.LessonPlanItem x = new AiGeneratedRoadmapResponse.LessonPlanItem();
                x.setTitle((item.getTitle() == null || item.getTitle().isBlank()) ? "Bài học" : item.getTitle());
                x.setSummary((item.getSummary() == null || item.getSummary().isBlank())
                        ? "Nội dung theo chuẩn " + level : item.getSummary());
                x.setDuration((item.getDuration() == null || item.getDuration().isBlank()) ? "45 phút" : item.getDuration());
                out.add(x);
                if (out.size() >= 10) break;
            }
        }
        while (out.size() < 10) {
            int idx = out.size();
            String skill = (focusSkills == null || focusSkills.isEmpty())
                    ? "kỹ năng trọng tâm"
                    : focusSkills.get(idx % focusSkills.size());
            AiGeneratedRoadmapResponse.LessonPlanItem x = new AiGeneratedRoadmapResponse.LessonPlanItem();
            x.setTitle("Bài " + String.format("%02d", idx + 1) + " - " + moduleTitle);
            x.setSummary("Luyện " + skill + " theo mức " + level + ".");
            x.setDuration("45 phút");
            out.add(x);
        }
        for (int i = 0; i < out.size(); i++) {
            out.get(i).setLessonNo(i + 1);
        }
        return out;
    }

    private int defaultTargetByLevel(String level) {
        return switch (level == null ? "L1" : level) {
            case "L3" -> 85;
            case "L2" -> 70;
            default -> 55;
        };
    }

    private List<String> pickFocusSkills(List<String> skills, int offset) {
        if (skills == null || skills.isEmpty()) {
            return List.of("Kỹ năng trọng tâm");
        }
        List<String> out = new ArrayList<>();
        out.add(skills.get(offset % skills.size()));
        if (skills.size() > 1) {
            out.add(skills.get((offset + 1) % skills.size()));
        }
        return out;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserProfile getOrCreateProfile(User user) {
        Optional<UserProfile> found = userProfileRepository.findByUser_Id(user.getId());
        if (found.isPresent()) {
            return found.get();
        }
        UserProfile created = new UserProfile();
        created.setUser(user);
        created.setLearningGoal("Reach target level in all subscribed subjects.");
        created.setTargetScore(75);
        created.setNotifyEmail(false);
        created.setNotifyInApp(true);
        created.setAcademicTrack("GRADE_11");
        created.setAcademicTrackConfirmed(false);
        created.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(created);
    }

    private Map<String, Object> profilePayload(User user, UserProfile profile) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", user.getId());
        payload.put("email", user.getEmail());
        payload.put("fullName", user.getFullName() == null ? "" : user.getFullName());
        payload.put("role", roleAccessService.resolveRoleName(user));
        payload.put("learningGoal", profile.getLearningGoal() == null ? "" : profile.getLearningGoal());
        payload.put("targetScore", profile.getTargetScore() == null ? 75 : profile.getTargetScore());
        payload.put("academicTrack", profile.getAcademicTrack() == null ? "GRADE_11" : profile.getAcademicTrack());
        payload.put("academicTrackConfirmed", profile.isAcademicTrackConfirmed());
        payload.put("notifyEmail", profile.isNotifyEmail());
        payload.put("notifyInApp", profile.isNotifyInApp());
        payload.put("activeSubjects", subscriptionRepository.findByUserIdAndIsActiveTrue(user.getId()).size());
        return payload;
    }

    private String normalizeAcademicTrack(String track) {
        String normalized = track == null ? "" : track.trim().toUpperCase();
        if (normalized.isBlank())
            return "GRADE_11";
        return switch (normalized) {
            case "GRADE_11", "GRADE_12", "UNI_PREP" -> normalized;
            default -> throw new RuntimeException("academicTrack must be GRADE_11, GRADE_12 or UNI_PREP");
        };
    }

    private record SkillPlan(String skillType, int count) {
    }

    private record RoadmapFramework(
            String code,
            String title,
            String description,
            List<String> modules,
            List<String> preferredSkills) {
    }
}
