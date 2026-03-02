package com.compassed.compassed_api.api.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.AdminCreateRoadmapRequest;
import com.compassed.compassed_api.api.dto.AdminReviewAiLogRequest;
import com.compassed.compassed_api.api.dto.AdminSendNotificationRequest;
import com.compassed.compassed_api.api.dto.AdminUpsertLessonRequest;
import com.compassed.compassed_api.api.dto.AdminUpsertMiniTestRequest;
import com.compassed.compassed_api.api.dto.AdminUpdateConfigRequest;
import com.compassed.compassed_api.api.dto.AdminUpdateRoadmapRequest;
import com.compassed.compassed_api.api.dto.AdminUpdateSubjectRequest;
import com.compassed.compassed_api.api.dto.AdminUpdateUserRoleRequest;
import com.compassed.compassed_api.domain.entity.Notification;
import com.compassed.compassed_api.domain.entity.Roadmap;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.SystemConfig;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.entity.Lesson;
import com.compassed.compassed_api.domain.entity.MiniTest;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.AiGenerationLogRepository;
import com.compassed.compassed_api.repository.LessonRepository;
import com.compassed.compassed_api.repository.MiniTestRepository;
import com.compassed.compassed_api.repository.NotificationRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.RoadmapRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.SystemConfigRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserRoadmapAssignmentRepository;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.PaymentService;
import com.compassed.compassed_api.service.RoleAccessService;

@RestController
@Profile("mysql")
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final RoadmapRepository roadmapRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlacementResultRepository placementResultRepository;
    private final UserRoadmapAssignmentRepository userRoadmapAssignmentRepository;
    private final NotificationRepository notificationRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final AiGenerationLogRepository aiGenerationLogRepository;
    private final LessonRepository lessonRepository;
    private final MiniTestRepository miniTestRepository;
    private final PaymentService paymentService;
    private final RoleAccessService roleAccessService;
    private final CurrentUserService currentUserService;

    public AdminController(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            RoadmapRepository roadmapRepository,
            SubscriptionRepository subscriptionRepository,
            PlacementResultRepository placementResultRepository,
            UserRoadmapAssignmentRepository userRoadmapAssignmentRepository,
            NotificationRepository notificationRepository,
            SystemConfigRepository systemConfigRepository,
            AiGenerationLogRepository aiGenerationLogRepository,
            LessonRepository lessonRepository,
            MiniTestRepository miniTestRepository,
            PaymentService paymentService,
            RoleAccessService roleAccessService,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.roadmapRepository = roadmapRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.placementResultRepository = placementResultRepository;
        this.userRoadmapAssignmentRepository = userRoadmapAssignmentRepository;
        this.notificationRepository = notificationRepository;
        this.systemConfigRepository = systemConfigRepository;
        this.aiGenerationLogRepository = aiGenerationLogRepository;
        this.lessonRepository = lessonRepository;
        this.miniTestRepository = miniTestRepository;
        this.paymentService = paymentService;
        this.roleAccessService = roleAccessService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/analytics/overview")
    public Map<String, Object> overview() {
        long totalUsers = userRepository.count();
        long totalSubjects = subjectRepository.count();
        long activeSubscriptions = subscriptionRepository.countByIsActiveTrue();
        long totalPlacementResults = placementResultRepository.count();
        long passedPlacements = placementResultRepository.countPassed();
        Double avgScore = placementResultRepository.averageScorePercent();
        long roadmapAssignments = userRoadmapAssignmentRepository.count();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalUsers", totalUsers);
        response.put("totalSubjects", totalSubjects);
        response.put("activeSubscriptions", activeSubscriptions);
        response.put("totalPlacementResults", totalPlacementResults);
        response.put("passedPlacements", passedPlacements);
        response.put("passRatePercent",
                totalPlacementResults == 0 ? 0.0 : (passedPlacements * 100.0) / totalPlacementResults);
        response.put("averageScorePercent", avgScore == null ? 0.0 : avgScore);
        response.put("roadmapAssignments", roadmapAssignments);
        return response;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        return userRepository.findAll().stream().map(u -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", u.getId());
            row.put("email", u.getEmail());
            row.put("fullName", u.getFullName());
            row.put("role", roleAccessService.resolveRoleName(u));
            return row;
        }).toList();
    }

    @PutMapping("/users/{userId}/role")
    public Map<String, Object> updateUserRole(@PathVariable Long userId, @RequestBody AdminUpdateUserRoleRequest request) {
        if (request == null || request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("role is required");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("role must be USER or ADMIN");
        }
        roleAccessService.assignRole(user, role);
        return Map.of("id", user.getId(), "role", role.name());
    }

    @GetMapping("/subjects")
    public List<Subject> subjects() {
        return subjectRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @PostMapping("/subjects")
    public Subject createSubject(@RequestBody AdminUpdateSubjectRequest request) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()
                || request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("code and name are required");
        }
        Subject subject = new Subject();
        subject.setCode(request.getCode().trim().toUpperCase());
        subject.setName(request.getName().trim());
        return subjectRepository.save(subject);
    }

    @PutMapping("/subjects/{subjectId}")
    public Subject updateSubject(@PathVariable Long subjectId, @RequestBody AdminUpdateSubjectRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        if (request.getCode() != null && !request.getCode().isBlank()) {
            subject.setCode(request.getCode().trim().toUpperCase());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            subject.setName(request.getName().trim());
        }
        return subjectRepository.save(subject);
    }

    @DeleteMapping("/subjects/{subjectId}")
    public Map<String, Object> deleteSubject(@PathVariable Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        subjectRepository.delete(subject);
        return Map.of("deleted", true, "id", subjectId);
    }

    @GetMapping("/roadmaps")
    public List<Roadmap> roadmaps() {
        return roadmapRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    @PostMapping("/roadmaps")
    public Roadmap createRoadmap(@RequestBody AdminCreateRoadmapRequest request) {
        if (request == null || request.getSubjectId() == null || request.getLevel() == null || request.getLevel().isBlank()
                || request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("subjectId, level and title are required");
        }
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        Level level;
        try {
            level = Level.valueOf(request.getLevel().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("level must be L1/L2/L3");
        }
        Roadmap roadmap = new Roadmap();
        roadmap.setSubject(subject);
        roadmap.setLevel(level);
        roadmap.setTitle(request.getTitle().trim());
        roadmap.setDescription(request.getDescription());
        roadmap.setDisplayOrder(request.getDisplayOrder());
        return roadmapRepository.save(roadmap);
    }

    @PutMapping("/roadmaps/{roadmapId}")
    public Roadmap updateRoadmap(@PathVariable Long roadmapId, @RequestBody AdminUpdateRoadmapRequest request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new RuntimeException("Roadmap not found"));
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            roadmap.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            roadmap.setDescription(request.getDescription());
        }
        if (request.getDisplayOrder() != null) {
            roadmap.setDisplayOrder(request.getDisplayOrder());
        }
        return roadmapRepository.save(roadmap);
    }

    @DeleteMapping("/roadmaps/{roadmapId}")
    public Map<String, Object> deleteRoadmap(@PathVariable Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new RuntimeException("Roadmap not found"));
        roadmapRepository.delete(roadmap);
        return Map.of("deleted", true, "id", roadmapId);
    }

    @GetMapping("/lessons")
    public List<Lesson> lessons() {
        return lessonRepository.findAll().stream()
                .sorted((a, b) -> {
                    int bySubject = a.getSubject().compareToIgnoreCase(b.getSubject());
                    if (bySubject != 0) {
                        return bySubject;
                    }
                    int byLevel = a.getLevel().compareToIgnoreCase(b.getLevel());
                    if (byLevel != 0) {
                        return byLevel;
                    }
                    return Integer.compare(a.getOrderIndex(), b.getOrderIndex());
                })
                .toList();
    }

    @PostMapping("/lessons")
    public Lesson createLesson(@RequestBody AdminUpsertLessonRequest request) {
        Lesson lesson = new Lesson();
        applyLesson(lesson, request, true);
        return lessonRepository.save(lesson);
    }

    @PutMapping("/lessons/{lessonId}")
    public Lesson updateLesson(@PathVariable Long lessonId, @RequestBody AdminUpsertLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        applyLesson(lesson, request, false);
        return lessonRepository.save(lesson);
    }

    @DeleteMapping("/lessons/{lessonId}")
    public Map<String, Object> deleteLesson(@PathVariable Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        lessonRepository.delete(lesson);
        return Map.of("deleted", true, "id", lessonId);
    }

    @GetMapping("/mini-tests")
    public List<MiniTest> miniTests() {
        return miniTestRepository.findAll().stream()
                .sorted((a, b) -> {
                    int bySubject = a.getSubject().compareToIgnoreCase(b.getSubject());
                    if (bySubject != 0) {
                        return bySubject;
                    }
                    int byLevel = a.getLevel().compareToIgnoreCase(b.getLevel());
                    if (byLevel != 0) {
                        return byLevel;
                    }
                    return Integer.compare(a.getLessonId(), b.getLessonId());
                })
                .toList();
    }

    @PostMapping("/mini-tests")
    public MiniTest createMiniTest(@RequestBody AdminUpsertMiniTestRequest request) {
        MiniTest miniTest = new MiniTest();
        applyMiniTest(miniTest, request, true);
        return miniTestRepository.save(miniTest);
    }

    @PutMapping("/mini-tests/{miniTestId}")
    public MiniTest updateMiniTest(@PathVariable Long miniTestId, @RequestBody AdminUpsertMiniTestRequest request) {
        MiniTest miniTest = miniTestRepository.findById(miniTestId)
                .orElseThrow(() -> new RuntimeException("Mini test not found"));
        applyMiniTest(miniTest, request, false);
        return miniTestRepository.save(miniTest);
    }

    @DeleteMapping("/mini-tests/{miniTestId}")
    public Map<String, Object> deleteMiniTest(@PathVariable Long miniTestId) {
        MiniTest miniTest = miniTestRepository.findById(miniTestId)
                .orElseThrow(() -> new RuntimeException("Mini test not found"));
        miniTestRepository.delete(miniTest);
        return Map.of("deleted", true, "id", miniTestId);
    }

    @GetMapping("/configs")
    public List<SystemConfig> configs() {
        return systemConfigRepository.findAll().stream()
                .sorted((a, b) -> a.getConfigKey().compareToIgnoreCase(b.getConfigKey()))
                .toList();
    }

    @PutMapping("/configs/{configKey}")
    public SystemConfig updateConfig(@PathVariable String configKey, @RequestBody AdminUpdateConfigRequest request) {
        if (request == null || request.getValue() == null) {
            throw new RuntimeException("Config value is required");
        }
        SystemConfig cfg = systemConfigRepository.findByConfigKey(configKey)
                .orElseGet(SystemConfig::new);
        cfg.setConfigKey(configKey);
        cfg.setConfigValue(request.getValue());
        cfg.setUpdatedAt(LocalDateTime.now());
        return systemConfigRepository.save(cfg);
    }

    @DeleteMapping("/configs/{configKey}")
    public Map<String, Object> deleteConfig(@PathVariable String configKey) {
        SystemConfig cfg = systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new RuntimeException("Config not found"));
        systemConfigRepository.delete(cfg);
        return Map.of("deleted", true, "configKey", configKey);
    }

    @PostMapping("/notifications")
    public Map<String, Object> sendNotification(@RequestBody AdminSendNotificationRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()
                || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new RuntimeException("title and message are required");
        }
        String type = (request.getType() == null || request.getType().isBlank()) ? "GENERAL" : request.getType().trim();

        int created = 0;
        if (request.isBroadcast()) {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                created += createNotification(user, request.getTitle(), request.getMessage(), type);
            }
        } else {
            if (request.getUserId() == null) {
                throw new RuntimeException("userId is required when broadcast=false");
            }
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            created += createNotification(user, request.getTitle(), request.getMessage(), type);
        }
        return Map.of("created", created);
    }

    @GetMapping("/notifications")
    public List<Map<String, Object>> notifications() {
        return notificationRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(200)
                .map(n -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", n.getId());
                    item.put("userId", n.getUser().getId());
                    item.put("userEmail", n.getUser().getEmail());
                    item.put("title", n.getTitle());
                    item.put("message", n.getMessage());
                    item.put("type", n.getType());
                    item.put("read", n.isReadFlag());
                    item.put("createdAt", n.getCreatedAt());
                    return item;
                }).toList();
    }

    @GetMapping("/payments")
    public List<Map<String, Object>> payments(@RequestParam(required = false) String status) {
        return paymentService.getPaymentsForAdmin(status);
    }

    @PostMapping("/payments/{paymentId}/approve")
    public Map<String, Object> approvePayment(@PathVariable Long paymentId) {
        return paymentService.approveManualPayment(paymentId);
    }

    @PostMapping("/payments/{paymentId}/reject")
    public Map<String, Object> rejectPayment(
            @PathVariable Long paymentId,
            @RequestBody(required = false) Map<String, Object> request) {
        String reason = request == null ? null : String.valueOf(request.getOrDefault("reason", ""));
        return paymentService.rejectManualPayment(paymentId, reason);
    }

    @GetMapping("/ai/logs")
    public List<Map<String, Object>> aiLogs() {
        return aiGenerationLogRepository.findTop100ByOrderByCreatedAtDesc().stream().map(log -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", log.getId());
            row.put("taskType", log.getTaskType());
            row.put("subjectCode", log.getSubjectCode());
            row.put("reviewStatus", log.getReviewStatus());
            row.put("reviewNote", log.getReviewNote());
            row.put("reviewedByUserId", log.getReviewedByUserId());
            row.put("createdAt", log.getCreatedAt());
            row.put("reviewedAt", log.getReviewedAt());
            row.put("outputText", log.getOutputText());
            return row;
        }).toList();
    }

    @PostMapping("/ai/logs/{id}/review")
    public Map<String, Object> reviewAiLog(@PathVariable Long id, @RequestBody AdminReviewAiLogRequest request) {
        if (request == null || request.getReviewStatus() == null || request.getReviewStatus().isBlank()) {
            throw new RuntimeException("reviewStatus is required");
        }
        var log = aiGenerationLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI log not found"));
        log.setReviewStatus(request.getReviewStatus().trim().toUpperCase());
        log.setReviewNote(request.getReviewNote());
        log.setReviewedAt(LocalDateTime.now());
        log.setReviewedByUserId(currentUserService.requireCurrentUserId());
        aiGenerationLogRepository.save(log);
        return Map.of("id", log.getId(), "reviewStatus", log.getReviewStatus());
    }

    private int createNotification(User user, String title, String message, String type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title.trim());
        n.setMessage(message.trim());
        n.setType(type);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
        return 1;
    }

    private void applyLesson(Lesson lesson, AdminUpsertLessonRequest request, boolean createMode) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }
        if (createMode || request.getTitle() != null) {
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                throw new RuntimeException("title is required");
            }
            lesson.setTitle(request.getTitle().trim());
        }
        if (createMode || request.getSubject() != null) {
            if (request.getSubject() == null || request.getSubject().isBlank()) {
                throw new RuntimeException("subject is required");
            }
            lesson.setSubject(request.getSubject().trim().toUpperCase());
        }
        if (createMode || request.getLevel() != null) {
            if (request.getLevel() == null || request.getLevel().isBlank()) {
                throw new RuntimeException("level is required");
            }
            lesson.setLevel(request.getLevel().trim().toUpperCase());
        }
        if (createMode || request.getOrderIndex() != null) {
            if (request.getOrderIndex() == null || request.getOrderIndex() < 1) {
                throw new RuntimeException("orderIndex must be >= 1");
            }
            lesson.setOrderIndex(request.getOrderIndex());
        }
        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }
    }

    private void applyMiniTest(MiniTest miniTest, AdminUpsertMiniTestRequest request, boolean createMode) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }
        if (createMode || request.getTitle() != null) {
            if (request.getTitle() == null || request.getTitle().isBlank()) {
                throw new RuntimeException("title is required");
            }
            miniTest.setTitle(request.getTitle().trim());
        }
        if (createMode || request.getSubject() != null) {
            if (request.getSubject() == null || request.getSubject().isBlank()) {
                throw new RuntimeException("subject is required");
            }
            miniTest.setSubject(request.getSubject().trim().toUpperCase());
        }
        if (createMode || request.getLevel() != null) {
            if (request.getLevel() == null || request.getLevel().isBlank()) {
                throw new RuntimeException("level is required");
            }
            miniTest.setLevel(request.getLevel().trim().toUpperCase());
        }
        if (createMode || request.getLessonId() != null) {
            if (request.getLessonId() == null || request.getLessonId() < 1) {
                throw new RuntimeException("lessonId must be >= 1");
            }
            miniTest.setLessonId(request.getLessonId());
        }
        if (request.getQuestions() != null) {
            miniTest.setQuestions(request.getQuestions());
        }
    }
}
