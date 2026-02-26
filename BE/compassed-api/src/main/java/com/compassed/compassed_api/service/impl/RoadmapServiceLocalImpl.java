package com.compassed.compassed_api.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.FinalTestSubmitRequest;
import com.compassed.compassed_api.api.dto.LessonCompleteRequest;
import com.compassed.compassed_api.api.dto.LessonResponse;
import com.compassed.compassed_api.api.dto.MiniTestResponse;
import com.compassed.compassed_api.api.dto.MiniTestSubmitRequest;
import com.compassed.compassed_api.api.dto.RoadmapResponse;
import com.compassed.compassed_api.domain.enums.Level;
import com.compassed.compassed_api.local.LessonBank;
import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.local.LocalDataStore.RoadmapProgressMem;
import com.compassed.compassed_api.local.LocalDataStore.SubjectInfo;
import com.compassed.compassed_api.service.RoadmapService;

@Service
@Profile("local")
public class RoadmapServiceLocalImpl implements RoadmapService {

    private final LocalDataStore localDataStore;

    public RoadmapServiceLocalImpl(LocalDataStore localDataStore) {
        this.localDataStore = localDataStore;
    }

    @Override
    public RoadmapResponse getRoadmap(Long userId, Long subjectId) {
        ensureUserExists(userId);
        SubjectInfo subject = getSubjectOrThrow(subjectId);

        RoadmapResponse response = baseResponse(subject);
        response.setSubscribed(localDataStore.hasActiveSubscription(userId, subjectId));

        var placement = localDataStore.getLatestResult(userId, subjectId);
        response.setPlacementReady(placement != null);

        if (!response.getSubscribed()) {
            response.setPhase("LOCKED");
            response.setNextStep("SUBSCRIBE_TO_UNLOCK_ROADMAP");
            response.setLessons(List.of());
            response.setMiniTests(List.of());
            response.setProgressPercent(0);
            return response;
        }

        if (!response.getPlacementReady()) {
            response.setPhase("WAITING_PLACEMENT");
            response.setNextStep("TAKE_PLACEMENT_TEST");
            response.setLessons(List.of());
            response.setMiniTests(List.of());
            response.setProgressPercent(0);
            return response;
        }

        RoadmapProgressMem progress = localDataStore.initializeRoadmapProgress(userId, subjectId, placement.getLevel());
        synchronized (progress) {
            refreshProgressState(progress, subject.code());
            return toResponse(subject, progress);
        }
    }

    @Override
    public void completeLesson(Long userId, Long lessonId, LessonCompleteRequest request) {
        if (request == null || request.getSubjectId() == null) {
            throw new RuntimeException("subjectId is required");
        }
        RoadmapContext context = requireActiveRoadmap(userId, request.getSubjectId());

        synchronized (context.progress()) {
            refreshProgressState(context.progress(), context.subject().code());
            if (!"LESSONS".equals(context.progress().getPhase())) {
                throw new RuntimeException("Lessons are not the current step");
            }

            String levelKey = context.progress().getCurrentLevel().name();
            List<LessonBank.LessonData> lessons = lessonsBy(context.subject().code(), levelKey);
            boolean validLesson = lessons.stream().anyMatch(l -> l.id().equals(lessonId));
            if (!validLesson) {
                throw new RuntimeException("Lesson not found in current roadmap level");
            }

            context.progress().getCompletedLessons(levelKey).add(lessonId);
            refreshProgressState(context.progress(), context.subject().code());
        }
    }

    @Override
    public void submitMiniTest(Long userId, Long subjectId, Long miniTestId, MiniTestSubmitRequest request) {
        RoadmapContext context = requireActiveRoadmap(userId, subjectId);
        Integer score = normalizeScore(request == null ? null : request.getScore());

        synchronized (context.progress()) {
            refreshProgressState(context.progress(), context.subject().code());
            if (!"MINI_TESTS".equals(context.progress().getPhase())) {
                throw new RuntimeException("Mini tests are not available yet");
            }

            String levelKey = context.progress().getCurrentLevel().name();
            List<LessonBank.LessonData> lessons = lessonsBy(context.subject().code(), levelKey);
            boolean validMiniTest = lessons.stream().anyMatch(l -> l.id().equals(miniTestId));
            if (!validMiniTest) {
                throw new RuntimeException("Mini test not found in current roadmap level");
            }
            if (!context.progress().getCompletedLessons(levelKey).contains(miniTestId)) {
                throw new RuntimeException("Complete lesson before taking mini test");
            }

            context.progress().getMiniScores(levelKey).put(miniTestId, score);
            refreshProgressState(context.progress(), context.subject().code());
        }
    }

    @Override
    public RoadmapResponse submitFinalTest(Long userId, Long subjectId, FinalTestSubmitRequest request) {
        RoadmapContext context = requireActiveRoadmap(userId, subjectId);
        Integer score = normalizeScore(request == null ? null : request.getScore());

        synchronized (context.progress()) {
            refreshProgressState(context.progress(), context.subject().code());
            if (!"FINAL_TEST".equals(context.progress().getPhase())) {
                throw new RuntimeException("Final test is not available yet");
            }

            String levelKey = context.progress().getCurrentLevel().name();
            context.progress().setFinalScore(levelKey, score);
            refreshProgressState(context.progress(), context.subject().code());
            return toResponse(context.subject(), context.progress());
        }
    }

    @Override
    public RoadmapResponse getLessonDetail(Long userId, Long lessonId) {
        ensureUserExists(userId);
        if (lessonId == null) {
            throw new RuntimeException("lessonId is required");
        }

        List<SubjectInfo> candidates = new ArrayList<>();
        for (SubjectInfo subject : localDataStore.getAllSubjects()) {
            if (!localDataStore.hasActiveSubscription(userId, subject.id())) {
                continue;
            }
            var placement = localDataStore.getLatestResult(userId, subject.id());
            if (placement == null) {
                continue;
            }

            RoadmapProgressMem progress = localDataStore.initializeRoadmapProgress(userId, subject.id(), placement.getLevel());
            synchronized (progress) {
                refreshProgressState(progress, subject.code());
                String levelKey = progress.getCurrentLevel().name();
                boolean exists = lessonsBy(subject.code(), levelKey).stream()
                        .anyMatch(lesson -> lesson.id().equals(lessonId));
                if (exists) {
                    candidates.add(subject);
                }
            }
        }

        if (candidates.isEmpty()) {
            throw new RuntimeException("Lesson not found in active roadmap");
        }
        if (candidates.size() > 1) {
            throw new RuntimeException("Ambiguous lessonId across subjects; use subject roadmap endpoint");
        }
        return getRoadmap(userId, candidates.get(0).id());
    }

    private RoadmapContext requireActiveRoadmap(Long userId, Long subjectId) {
        ensureUserExists(userId);
        SubjectInfo subject = getSubjectOrThrow(subjectId);

        if (!localDataStore.hasActiveSubscription(userId, subjectId)) {
            throw new RuntimeException("Need subscription to access roadmap");
        }

        var placement = localDataStore.getLatestResult(userId, subjectId);
        if (placement == null) {
            throw new RuntimeException("Need placement result before roadmap");
        }

        RoadmapProgressMem progress = localDataStore.initializeRoadmapProgress(userId, subjectId, placement.getLevel());
        return new RoadmapContext(subject, progress);
    }

    private void refreshProgressState(RoadmapProgressMem progress, String subjectCode) {
        String levelKey = progress.getCurrentLevel().name();
        List<LessonBank.LessonData> lessons = lessonsBy(subjectCode, levelKey);
        int totalLessons = lessons.size();

        Integer finalScore = progress.getFinalScore(levelKey);
        if (finalScore != null) {
            if (finalScore >= 70) {
                if (progress.getCurrentLevel() == Level.L3) {
                    progress.setPhase("COURSE_COMPLETED");
                    return;
                }
                progress.setCurrentLevel(nextLevel(progress.getCurrentLevel()));
                progress.setPhase("LESSONS");
                return;
            }

            progress.setReplanCount(progress.getReplanCount() + 1);
            progress.resetLevelProgress(levelKey);
            progress.setPhase("LESSONS");
            return;
        }

        int completedLessons = progress.getCompletedLessons(levelKey).size();
        int completedMiniTests = progress.getMiniScores(levelKey).size();

        if (completedLessons < totalLessons) {
            progress.setPhase("LESSONS");
            return;
        }
        if (completedMiniTests < totalLessons) {
            progress.setPhase("MINI_TESTS");
            return;
        }
        progress.setPhase("FINAL_TEST");
    }

    private RoadmapResponse toResponse(SubjectInfo subject, RoadmapProgressMem progress) {
        String levelKey = progress.getCurrentLevel().name();
        List<LessonBank.LessonData> lessonsData = lessonsBy(subject.code(), levelKey);

        List<LessonResponse> lessons = new ArrayList<>();
        for (LessonBank.LessonData lessonData : lessonsData) {
            boolean completed = progress.getCompletedLessons(levelKey).contains(lessonData.id());
            lessons.add(new LessonResponse(
                    lessonData.id(),
                    lessonData.title(),
                    lessonData.content(),
                    lessonData.displayOrder(),
                    lessonData.estimatedMinutes(),
                    completed));
        }

        Map<Long, Integer> miniScores = progress.getMiniScores(levelKey);
        List<MiniTestResponse> miniTests = new ArrayList<>();
        for (LessonBank.LessonData lessonData : lessonsData) {
            Integer score = miniScores.get(lessonData.id());
            miniTests.add(new MiniTestResponse(
                    lessonData.id(),
                    "Mini Test - " + lessonData.title(),
                    5,
                    lessonData.id().intValue(),
                    score != null,
                    score));
        }

        int totalLessons = lessonsData.size();
        int completedCount = progress.getCompletedLessons(levelKey).size();
        double avgMiniScore = miniScores.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);

        RoadmapResponse response = baseResponse(subject);
        response.setSubscribed(true);
        response.setPlacementReady(true);
        response.setLevel(levelKey);
        response.setLessons(lessons);
        response.setMiniTests(miniTests);
        response.setProgressPercent(totalLessons == 0 ? 0 : (int) ((completedCount * 100.0) / totalLessons));
        response.setMiniTestAverageScore(miniScores.isEmpty() ? null : avgMiniScore);
        response.setFinalTestScore(progress.getFinalScore(levelKey));
        response.setReplanCount(progress.getReplanCount());
        response.setPhase(progress.getPhase());
        response.setNextStep(nextStepByPhase(progress.getPhase()));
        return response;
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

    private Integer normalizeScore(Integer score) {
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

    private SubjectInfo getSubjectOrThrow(Long subjectId) {
        SubjectInfo subject = localDataStore.getSubject(subjectId);
        if (subject == null) {
            throw new RuntimeException("Subject not found");
        }
        return subject;
    }

    private void ensureUserExists(Long userId) {
        if (!localDataStore.userExists(userId)) {
            throw new RuntimeException("User not found");
        }
    }

    private RoadmapResponse baseResponse(SubjectInfo subject) {
        RoadmapResponse response = new RoadmapResponse();
        response.setSubjectId(subject.id());
        response.setSubjectCode(subject.code());
        response.setSubjectName(subject.name());
        return response;
    }

    private record RoadmapContext(SubjectInfo subject, RoadmapProgressMem progress) {
    }
}

