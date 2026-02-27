package com.compassed.compassed_api.api.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PlacementHistoryResponse;
import com.compassed.compassed_api.api.dto.TestHistoryResponse;
import com.compassed.compassed_api.domain.entity.Subject;
import com.compassed.compassed_api.repository.FinalTestAttemptRepository;
import com.compassed.compassed_api.repository.FinalTestRepository;
import com.compassed.compassed_api.repository.MiniTestAttemptRepository;
import com.compassed.compassed_api.repository.MiniTestRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.repository.RoadmapRepository;
import com.compassed.compassed_api.repository.SubjectRepository;
import com.compassed.compassed_api.security.CurrentUserService;

@RestController
@Profile("mysql")
@RequestMapping("/api/history")
public class HistoryMysqlController {

    private final PlacementResultRepository placementResultRepository;
    private final MiniTestAttemptRepository miniTestAttemptRepository;
    private final FinalTestAttemptRepository finalTestAttemptRepository;
    private final MiniTestRepository miniTestRepository;
    private final FinalTestRepository finalTestRepository;
    private final RoadmapRepository roadmapRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUserService currentUserService;

    public HistoryMysqlController(
            PlacementResultRepository placementResultRepository,
            MiniTestAttemptRepository miniTestAttemptRepository,
            FinalTestAttemptRepository finalTestAttemptRepository,
            MiniTestRepository miniTestRepository,
            FinalTestRepository finalTestRepository,
            RoadmapRepository roadmapRepository,
            SubjectRepository subjectRepository,
            CurrentUserService currentUserService) {
        this.placementResultRepository = placementResultRepository;
        this.miniTestAttemptRepository = miniTestAttemptRepository;
        this.finalTestAttemptRepository = finalTestAttemptRepository;
        this.miniTestRepository = miniTestRepository;
        this.finalTestRepository = finalTestRepository;
        this.roadmapRepository = roadmapRepository;
        this.subjectRepository = subjectRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/placements")
    public List<PlacementHistoryResponse> placementHistory() {
        Long userId = currentUserService.requireCurrentUserId();
        return placementResultRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream().map(item -> {
            PlacementHistoryResponse response = new PlacementHistoryResponse();
            response.setAttemptId(0L);
            response.setSubjectId(item.getSubject().getId());
            response.setSubjectCode(item.getSubject().getCode());
            response.setSubjectName(item.getSubject().getName());
            response.setScorePercent(item.getScorePercent());
            response.setLevel(item.getLevel() == null ? null : item.getLevel().name());
            response.setSubmittedAt(item.getCreatedAt());
            return response;
        }).toList();
    }

    @GetMapping("/tests")
    public List<TestHistoryResponse> allTestHistory() {
        Long userId = currentUserService.requireCurrentUserId();
        List<TestHistoryResponse> rows = new ArrayList<>();

        placementResultRepository.findByUser_IdOrderByCreatedAtDesc(userId).forEach(item -> {
            TestHistoryResponse r = new TestHistoryResponse();
            r.setTestName("Placement Test");
            r.setSubjectCode(item.getSubject().getCode());
            r.setSubjectName(item.getSubject().getName());
            r.setLevel(item.getLevel() == null ? null : item.getLevel().name());
            r.setSubmittedAt(item.getCreatedAt());
            r.setScorePercent(item.getScorePercent());
            rows.add(r);
        });

        miniTestAttemptRepository.findByUserIdOrderBySubmittedAtDesc(userId).forEach(attempt -> {
            miniTestRepository.findById(attempt.getMiniTestId()).ifPresent(mini -> {
                TestHistoryResponse r = new TestHistoryResponse();
                r.setTestName(mini.getTitle() == null || mini.getTitle().isBlank() ? "Mini Test" : mini.getTitle());
                Subject subject = resolveSubjectByCode(mini.getSubject());
                if (subject != null) {
                    r.setSubjectCode(subject.getCode());
                    r.setSubjectName(subject.getName());
                } else {
                    r.setSubjectCode(mini.getSubject());
                    r.setSubjectName(mini.getSubject());
                }
                r.setLevel(mini.getLevel());
                r.setSubmittedAt(attempt.getSubmittedAt());
                r.setScorePercent(attempt.getScore() == null ? 0.0 : attempt.getScore().doubleValue());
                rows.add(r);
            });
        });

        finalTestAttemptRepository.findByUserIdOrderBySubmittedAtDesc(userId).forEach(attempt -> {
            finalTestRepository.findById(attempt.getFinalTestId()).ifPresent(ft -> {
                TestHistoryResponse r = new TestHistoryResponse();
                r.setTestName(ft.getTitle() == null || ft.getTitle().isBlank() ? "Final Test" : ft.getTitle());
                roadmapRepository.findById(ft.getRoadmapId()).ifPresent(roadmap -> {
                    r.setSubjectCode(roadmap.getSubject().getCode());
                    r.setSubjectName(roadmap.getSubject().getName());
                    r.setLevel(roadmap.getLevel() == null ? null : roadmap.getLevel().name());
                });
                r.setSubmittedAt(attempt.getSubmittedAt());
                r.setScorePercent(attempt.getScore() == null ? 0.0 : attempt.getScore().doubleValue());
                rows.add(r);
            });
        });

        rows.sort(Comparator.comparing(TestHistoryResponse::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return rows;
    }

    private Subject resolveSubjectByCode(String code) {
        if (code == null || code.isBlank()) return null;
        Optional<Subject> direct = subjectRepository.findByCode(code);
        if (direct.isPresent()) return direct.get();
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        return subjectRepository.findAll().stream()
                .filter(s -> s.getCode() != null && s.getCode().trim().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst()
                .orElse(null);
    }
}
