package com.compassed.compassed_api.api.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PlacementStartResponse;
import com.compassed.compassed_api.api.dto.PlacementSubmitRequest;
import com.compassed.compassed_api.api.dto.PlacementSubmitResponse;
import com.compassed.compassed_api.domain.enums.AttemptStatus;
import com.compassed.compassed_api.repository.PlacementAttemptRepository;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.PlacementService;

@RestController
@RequestMapping("/api")
public class PlacementController {

    private final PlacementService placementService;
    private final CurrentUserService currentUserService;
    private final PlacementAttemptRepository placementAttemptRepository;
    private final PlacementResultRepository placementResultRepository;

    public PlacementController(
            PlacementService placementService,
            CurrentUserService currentUserService,
            PlacementAttemptRepository placementAttemptRepository,
            PlacementResultRepository placementResultRepository) {
        this.placementService = placementService;
        this.currentUserService = currentUserService;
        this.placementAttemptRepository = placementAttemptRepository;
        this.placementResultRepository = placementResultRepository;
    }

    // Start placement
    @PostMapping("/subjects/{subjectId}/placement-tests")
    public PlacementStartResponse start(
            @PathVariable Long subjectId,
            @RequestParam(required = false) Integer gradeLevel,
            @RequestParam(required = false) String gradeBand
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return placementService.startPlacement(userId, subjectId, gradeLevel, gradeBand);
    }

    // Submit placement
    @PutMapping("/placement-attempts/{attemptId}/progress")
    public Map<String, Object> saveProgress(
            @PathVariable Long attemptId,
            @RequestBody PlacementSubmitRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        placementService.saveProgress(userId, attemptId, request);
        return Map.of("saved", true);
    }

    // Submit placement
    @PostMapping("/placement-attempts/{attemptId}/submit")
    public PlacementSubmitResponse submit(
            @PathVariable Long attemptId,
            @RequestBody PlacementSubmitRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return placementService.submitPlacement(userId, attemptId, request);
    }

    @GetMapping("/placement-attempts/latest-status")
    public Map<String, Object> latestStatus() {
        Long userId = currentUserService.requireCurrentUserId();
        return placementAttemptRepository.findTopByUser_IdOrderByStartedAtDesc(userId)
                .map(attempt -> {
                    String normalizedStatus = attempt.getStatus() == AttemptStatus.IN_PROGRESS ? "IN_PROGRESS" : "SUBMITTED";
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("status", normalizedStatus);
                    payload.put("attemptId", attempt.getId());
                    payload.put("subjectId", attempt.getSubject().getId());
                    return payload;
                })
                .orElseGet(() -> Map.of("status", "NOT_STARTED"));
    }

    @GetMapping("/subjects/{subjectId}/placement-result-status")
    public Map<String, Object> placementResultStatus(@PathVariable Long subjectId) {
        Long userId = currentUserService.requireCurrentUserId();
        return placementResultRepository.findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(userId, subjectId)
                .map(result -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("hasPlacementResult", true);
                    payload.put("level", result.getLevel() == null ? null : result.getLevel().name());
                    payload.put("scorePercent", result.getScorePercent() == null ? 0 : result.getScorePercent());
                    payload.put("skillAnalysisJson", result.getSkillAnalysisJson());
                    return payload;
                })
                .orElseGet(() -> Map.of("hasPlacementResult", false));
    }
}
