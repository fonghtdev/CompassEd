package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.FinalTestSubmitRequest;
import com.compassed.compassed_api.api.dto.LessonCompleteRequest;
import com.compassed.compassed_api.api.dto.LessonResponse;
import com.compassed.compassed_api.api.dto.MiniTestResponse;
import com.compassed.compassed_api.api.dto.MiniTestSubmitRequest;
import com.compassed.compassed_api.api.dto.RoadmapResponse;
import com.compassed.compassed_api.domain.entity.PlacementResult;
import com.compassed.compassed_api.domain.entity.Roadmap;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.domain.entity.UserProgress;
import com.compassed.compassed_api.domain.entity.UserRoadmapAssignment;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.local.LessonBank;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.RoadmapRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.repository.SubscriptionRepository;
import com.compassed.compassed_api.repository.UserProgressRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserRoadmapAssignmentRepository;
import com.compassed.compassed_api.service.RoadmapService;

@Service
@Profile("mysql")
public class RoadmapServiceMysqlImpl implements RoadmapService {

    private static final long FINAL_SCORE_LESSON_ID = 0L;

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlacementResultRepository placementResultRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRoadmapAssignmentRepository assignmentRepository;
    private final UserProgressRepository userProgressRepository;

    public RoadmapServiceMysqlImpl(
            UserRepository userRepository,
            SubjectRepository subjectRepository,
            SubscriptionRepository subscriptionRepository,
            PlacementResultRepository placementResultRepository,
            RoadmapRepository roadmapRepository,
            UserRoadmapAssignmentRepository assignmentRepository,
            UserProgressRepository userProgressRepository) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.placementResultRepository = placementResultRepository;
        this.roadmapRepository = roadmapRepository;
        this.assignmentRepository = assignmentRepository;
        this.userProgressRepository = userProgressRepository;
    }

    @Override
    public RoadmapResponse getRoadmap(Long userId, Long subjectId) {
        ensureUserExists(userId);
        Subject subject = getSubjectOrThrow(subjectId);

        RoadmapResponse response = baseResponse(subject);
        boolean subscribed = subscriptionRepository.existsByUserIdAndSubjectIdAndIsActiveTrue(userId, subjectId);
        response.setSubscribed(subscribed);

        PlacementResult placement = placementResultRepository.findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subjectId)
                .orElse(null);
        response.setPlacementReady(placement != null);

        if (!subscribed) {
            response.setPhase("LOCKED");
            response.setNextStep("SUBSCRIBE_TO_UNLOCK_ROADMAP");
            response.setLessons(List.of());
            response.setMiniTests(List.of());
            response.setProgressPercent(0);
            return response;
        }
        if (placement == null) {
            response.setPhase("WAITING_PLACEMENT");
            response.setNextStep("TAKE_PLACEMENT_TEST");
            response.setLessons(List.of());
            response.setMiniTests(List.of());
            response.setProgressPercent(0);
            return response;
        }

        UserRoadmapAssignment assignment = getOrCreateAssignment(userId, subject, placement.getLevel());
        synchronized (assignment) {
            refreshProgressState(userId, subject, assignment);
            assignmentRepository.save(assignment);
            return toResponse(userId, subject, assignment);
        }
    }

    @Override
    public void completeLesson(Long userId, Long lessonId, LessonCompleteRequest request) {
        if (request == null || request.getSubjectId() == null) {
            throw new RuntimeException("subjectId is required");
        }
        RoadmapContext context = requireActiveRoadmap(userId, request.getSubjectId());
        synchronized (context.assignment()) {
            refreshProgressState(userId, context.subject(), context.assignment());
            if (!"LESSONS".equals(context.assignment().getPhase())) {
                throw new RuntimeException("Lessons are not the current step");
            }
            validateLessonExists(context.subject(), context.assignment().getRoadmap().getLevel(), lessonId);
            String levelKey = context.assignment().getRoadmap().getLevel().name();

            UserProgress progress = userProgressRepository
                    .findByUserIdAndSubjectAndLevelAndLessonId(
                            userId,
                            context.subject().getCode(),
                            levelKey,
                            lessonId)
                    .orElseGet(UserProgress::new);
            progress.setUserId(userId);
            progress.setSubject(context.subject().getCode());
            progress.setLevel(levelKey);
            progress.setLessonId(lessonId);
            progress.setCompleted(true);
            userProgressRepository.save(progress);

            refreshProgressState(userId, context.subject(), context.assignment());
            assignmentRepository.save(context.assignment());
        }
    }

    @Override
    public void submitMiniTest(Long userId, Long subjectId, Long miniTestId, MiniTestSubmitRequest request) {
        RoadmapContext context = requireActiveRoadmap(userId, subjectId);
        int score = normalizeScore(request == null ? null : request.getScore());

        synchronized (context.assignment()) {
            refreshProgressState(userId, context.subject(), context.assignment());
            if (!"MINI_TESTS".equals(context.assignment().getPhase())) {
                throw new RuntimeException("Mini tests are not available yet");
            }
            validateLessonExists(context.subject(), context.assignment().getRoadmap().getLevel(), miniTestId);
            String levelKey = context.assignment().getRoadmap().getLevel().name();

            UserProgress progress = userProgressRepository
                    .findByUserIdAndSubjectAndLevelAndLessonId(
                            userId,
                            context.subject().getCode(),
                            levelKey,
                            miniTestId)
                    .orElseThrow(() -> new RuntimeException("Complete lesson before taking mini test"));
            if (!Boolean.TRUE.equals(progress.getCompleted())) {
                throw new RuntimeException("Complete lesson before taking mini test");
            }
            progress.setScore(score);
            userProgressRepository.save(progress);

            refreshProgressState(userId, context.subject(), context.assignment());
            assignmentRepository.save(context.assignment());
        }
    }

    @Override
    public RoadmapResponse submitFinalTest(Long userId, Long subjectId, FinalTestSubmitRequest request) {
        RoadmapContext context = requireActiveRoadmap(userId, subjectId);
        int score = normalizeScore(request == null ? null : request.getScore());

        synchronized (context.assignment()) {
            refreshProgressState(userId, context.subject(), context.assignment());
            if (!"FINAL_TEST".equals(context.assignment().getPhase())) {
                throw new RuntimeException("Final test is not available yet");
            }
            String levelKey = context.assignment().getRoadmap().getLevel().name();

            UserProgress finalProgress = userProgressRepository
                    .findByUserIdAndSubjectAndLevelAndLessonId(
                            userId,
                            context.subject().getCode(),
                            levelKey,
                            FINAL_SCORE_LESSON_ID)
                    .orElseGet(UserProgress::new);
            finalProgress.setUserId(userId);
            finalProgress.setSubject(context.subject().getCode());
            finalProgress.setLevel(levelKey);
            finalProgress.setLessonId(FINAL_SCORE_LESSON_ID);
            finalProgress.setCompleted(true);
            finalProgress.setScore(score);
            userProgressRepository.save(finalProgress);

            refreshProgressState(userId, context.subject(), context.assignment());
            assignmentRepository.save(context.assignment());
            return toResponse(userId, context.subject(), context.assignment());
        }
    }

    @Override
    public RoadmapResponse getLessonDetail(Long userId, Long lessonId) {
        ensureUserExists(userId);
        if (lessonId == null) {
            throw new RuntimeException("lessonId is required");
        }

        List<Subject> candidates = new ArrayList<>();
        for (Subject subject : subjectRepository.findAll()) {
            if (!subscriptionRepository.existsByUserIdAndSubjectIdAndIsActiveTrue(userId, subject.getId())) {
                continue;
            }
            PlacementResult placement = placementResultRepository
                    .findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subject.getId())
                    .orElse(null);
            if (placement == null) {
                continue;
            }
            UserRoadmapAssignment assignment = getOrCreateAssignment(userId, subject, placement.getLevel());
            Level level = assignment.getRoadmap().getLevel();
            boolean found = lessonsBy(subject.getCode(), level.name()).stream()
                    .anyMatch(lesson -> lesson.id().equals(lessonId));
            if (found) {
                candidates.add(subject);
            }
        }

        if (candidates.isEmpty()) {
            throw new RuntimeException("Lesson not found in active roadmap");
        }
        if (candidates.size() > 1) {
            throw new RuntimeException("Ambiguous lessonId across subjects; use subject roadmap endpoint");
        }
        return getRoadmap(userId, candidates.get(0).getId());
    }

    private UserRoadmapAssignment getOrCreateAssignment(Long userId, Subject subject, Level level) {
        return assignmentRepository.findByUserIdAndSubjectId(userId, subject.getId())
                .orElseGet(() -> {
                    Roadmap roadmap = roadmapRepository.findBySubject_IdAndLevel(subject.getId(), level)
                            .orElseThrow(() -> new RuntimeException(
                                    "Roadmap not found for subject=" + subject.getId() + ", level=" + level));
                    UserRoadmapAssignment assignment = new UserRoadmapAssignment();
                    assignment.setUser(userRepository.findById(userId).orElseThrow());
                    assignment.setSubject(subject);
                    assignment.setRoadmap(roadmap);
                    assignment.setAssignedAt(LocalDateTime.now());
                    assignment.setPhase("LESSONS");
                    assignment.setReplanCount(0);
                    return assignmentRepository.save(assignment);
                });
    }

    private void refreshProgressState(Long userId, Subject subject, UserRoadmapAssignment assignment) {
        String levelKey = assignment.getRoadmap().getLevel().name();
        List<LessonBank.LessonData> lessons = lessonsBy(subject.getCode(), levelKey);
        List<UserProgress> all = userProgressRepository.findByUserIdAndSubjectAndLevel(userId, subject.getCode(), levelKey);

        Integer finalScore = all.stream()
                .filter(p -> p.getLessonId() != null && p.getLessonId().equals(FINAL_SCORE_LESSON_ID))
                .map(UserProgress::getScore)
                .findFirst()
                .orElse(null);

        if (finalScore != null) {
            if (finalScore >= 70) {
                if (assignment.getRoadmap().getLevel() == Level.L3) {
                    assignment.setPhase("COURSE_COMPLETED");
                    return;
                }
                Level next = nextLevel(assignment.getRoadmap().getLevel());
                Roadmap nextRoadmap = roadmapRepository.findBySubject_IdAndLevel(subject.getId(), next)
                        .orElseThrow(() -> new RuntimeException("Roadmap not found for next level"));
                assignment.setRoadmap(nextRoadmap);
                assignment.setPhase("LESSONS");
                return;
            }

            assignment.setReplanCount(assignment.getReplanCount() + 1);
            userProgressRepository.deleteByUserIdAndSubjectAndLevel(userId, subject.getCode(), levelKey);
            assignment.setPhase("LESSONS");
            return;
        }

        Map<Long, UserProgress> byLesson = all.stream()
                .filter(p -> p.getLessonId() != null && p.getLessonId() > 0)
                .collect(Collectors.toMap(UserProgress::getLessonId, p -> p, (a, b) -> a));

        long completedLessons = lessons.stream()
                .filter(lesson -> {
                    UserProgress p = byLesson.get(lesson.id());
                    return p != null && Boolean.TRUE.equals(p.getCompleted());
                })
                .count();

        long completedMini = lessons.stream()
                .filter(lesson -> {
                    UserProgress p = byLesson.get(lesson.id());
                    return p != null && p.getScore() != null;
                })
                .count();

        if (completedLessons < lessons.size()) {
            assignment.setPhase("LESSONS");
            return;
        }
        if (completedMini < lessons.size()) {
            assignment.setPhase("MINI_TESTS");
            return;
        }
        assignment.setPhase("FINAL_TEST");
    }

    private RoadmapResponse toResponse(Long userId, Subject subject, UserRoadmapAssignment assignment) {
        String levelKey = assignment.getRoadmap().getLevel().name();
        List<LessonBank.LessonData> lessonData = lessonsBy(subject.getCode(), levelKey);
        List<UserProgress> all = userProgressRepository.findByUserIdAndSubjectAndLevel(userId, subject.getCode(), levelKey);
        Map<Long, UserProgress> byLesson = all.stream()
                .filter(p -> p.getLessonId() != null && p.getLessonId() > 0)
                .collect(Collectors.toMap(UserProgress::getLessonId, p -> p, (a, b) -> a));

        List<LessonResponse> lessons = new ArrayList<>();
        for (LessonBank.LessonData lesson : lessonData) {
            UserProgress p = byLesson.get(lesson.id());
            lessons.add(new LessonResponse(
                    lesson.id(),
                    lesson.title(),
                    lesson.content(),
                    lesson.displayOrder(),
                    lesson.estimatedMinutes(),
                    p != null && Boolean.TRUE.equals(p.getCompleted())));
        }

        List<MiniTestResponse> miniTests = new ArrayList<>();
        for (LessonBank.LessonData lesson : lessonData) {
            UserProgress p = byLesson.get(lesson.id());
            miniTests.add(new MiniTestResponse(
                    lesson.id(),
                    "Mini Test - " + lesson.title(),
                    5,
                    lesson.id().intValue(),
                    p != null && p.getScore() != null,
                    p == null ? null : p.getScore()));
        }

        Integer finalScore = userProgressRepository.findByUserIdAndSubjectAndLevelAndLessonId(
                userId,
                subject.getCode(),
                levelKey,
                FINAL_SCORE_LESSON_ID).map(UserProgress::getScore).orElse(null);

        long completedLessons = lessons.stream().filter(LessonResponse::getCompleted).count();
        double avgMini = miniTests.stream()
                .filter(m -> m.getScore() != null)
                .mapToInt(MiniTestResponse::getScore)
                .average()
                .orElse(0.0);

        RoadmapResponse response = baseResponse(subject);
        response.setSubscribed(true);
        response.setPlacementReady(true);
        response.setLevel(levelKey);
        response.setLessons(lessons);
        response.setMiniTests(miniTests);
        response.setFinalTestScore(finalScore);
        response.setReplanCount(assignment.getReplanCount());
        response.setMiniTestAverageScore(miniTests.stream().anyMatch(m -> m.getScore() != null) ? avgMini : null);
        response.setProgressPercent(lessonData.isEmpty() ? 0 : (int) ((completedLessons * 100.0) / lessonData.size()));
        response.setPhase(assignment.getPhase());
        response.setNextStep(nextStepByPhase(assignment.getPhase()));
        return response;
    }

    private RoadmapContext requireActiveRoadmap(Long userId, Long subjectId) {
        ensureUserExists(userId);
        Subject subject = getSubjectOrThrow(subjectId);
        if (!subscriptionRepository.existsByUserIdAndSubjectIdAndIsActiveTrue(userId, subjectId)) {
            throw new RuntimeException("Need subscription to access roadmap");
        }
        PlacementResult placement = placementResultRepository.findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subjectId)
                .orElseThrow(() -> new RuntimeException("Need placement result before roadmap"));
        UserRoadmapAssignment assignment = getOrCreateAssignment(userId, subject, placement.getLevel());
        return new RoadmapContext(subject, assignment);
    }

    private String nextStepByPhase(String phase) {
        if (phase == null) {
            return "COMPLETE_LESSONS";
        }
        return switch (phase) {
            case "LESSONS" -> "COMPLETE_LESSONS";
            case "MINI_TESTS" -> "TAKE_MINI_TESTS";
            case "FINAL_TEST" -> "TAKE_FINAL_TEST";
            case "COURSE_COMPLETED" -> "ROADMAP_COMPLETED";
            default -> "COMPLETE_LESSONS";
        };
    }

    private List<LessonBank.LessonData> lessonsBy(String subjectCode, String levelKey) {
        return LessonBank.getLessonsBySubjectAndLevel().getOrDefault(subjectCode + "_" + levelKey, List.of());
    }

    private void validateLessonExists(Subject subject, Level level, Long lessonId) {
        boolean found = lessonsBy(subject.getCode(), level.name()).stream().anyMatch(lesson -> lesson.id().equals(lessonId));
        if (!found) {
            throw new RuntimeException("Lesson not found in current roadmap level");
        }
    }

    private int normalizeScore(Integer score) {
        if (score == null) {
            throw new RuntimeException("Score is required");
        }
        if (score < 0 || score > 100) {
            throw new RuntimeException("Score must be between 0 and 100");
        }
        return score;
    }

    private Level nextLevel(Level currentLevel) {
        return switch (currentLevel) {
            case L1 -> Level.L2;
            case L2 -> Level.L3;
            case L3 -> Level.L3;
        };
    }

    private RoadmapResponse baseResponse(Subject subject) {
        RoadmapResponse response = new RoadmapResponse();
        response.setSubjectId(subject.getId());
        response.setSubjectCode(subject.getCode());
        response.setSubjectName(subject.getName());
        return response;
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
    }

    private Subject getSubjectOrThrow(Long subjectId) {
        return subjectRepository.findById(subjectId).orElseThrow(() -> new RuntimeException("Subject not found"));
    }

    private record RoadmapContext(Subject subject, UserRoadmapAssignment assignment) {
    }
}
