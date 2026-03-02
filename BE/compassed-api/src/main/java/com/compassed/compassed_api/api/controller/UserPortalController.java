package com.compassed.compassed_api.api.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.UpdateMyProfileRequest;
import com.compassed.compassed_api.api.dto.ChangeMyPasswordRequest;
import com.compassed.compassed_api.domain.entity.PlacementResult;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.Subscription;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.entity.UserProfile;
import com.compassed.compassed_api.domain.entity.UserRoadmapAssignment;
import com.compassed.compassed_api.local.QuestionBank;
import com.compassed.compassed_api.repository.NotificationRepository;
import com.compassed.compassed_api.repository.FinalTestAttemptRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.UserProfileRepository;
import com.compassed.compassed_api.repository.UserProgressRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserRoadmapAssignmentRepository;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.LoginActivityService;
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
    private final RoleAccessService roleAccessService;
    private final LoginActivityService loginActivityService;
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
            RoleAccessService roleAccessService,
            LoginActivityService loginActivityService,
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
        this.roleAccessService = roleAccessService;
        this.loginActivityService = loginActivityService;
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

    private Map<String, Object> buildProgressChart(List<PlacementResult> placements, List<UserRoadmapAssignment> assignments) {
        int roadmapInProgress = (int) assignments.stream().filter(a -> !"COURSE_COMPLETED".equals(a.getPhase())).count();
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

    private List<String> buildRecommendations(List<UserRoadmapAssignment> assignments, List<PlacementResult> placements) {
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
        created.setUpdatedAt(LocalDateTime.now());
        return userProfileRepository.save(created);
    }

    private Map<String, Object> profilePayload(User user, UserProfile profile) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "fullName", user.getFullName() == null ? "" : user.getFullName(),
                "role", roleAccessService.resolveRoleName(user),
                "learningGoal", profile.getLearningGoal() == null ? "" : profile.getLearningGoal(),
                "targetScore", profile.getTargetScore() == null ? 75 : profile.getTargetScore(),
                "notifyEmail", profile.isNotifyEmail(),
                "notifyInApp", profile.isNotifyInApp(),
                "activeSubjects", subscriptionRepository.findByUserIdAndIsActiveTrue(user.getId()).size());
    }
}
