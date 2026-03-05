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
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.PlacementService;

@RestController
@RequestMapping("/api")
public class PlacementController {

    private final PlacementService placementService;
    private final CurrentUserService currentUserService;
    private final PlacementAttemptRepository placementAttemptRepository;

    public PlacementController(
            PlacementService placementService,
            CurrentUserService currentUserService,
            PlacementAttemptRepository placementAttemptRepository) {
        this.placementService = placementService;
        this.currentUserService = currentUserService;
        this.placementAttemptRepository = placementAttemptRepository;
    }

    // Start placement
    @PostMapping("/subjects/{subjectId}/placement-tests")
    public PlacementStartResponse start(
            @PathVariable Long subjectId,
            @RequestParam(required = false) Integer gradeLevel
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return placementService.startPlacement(userId, subjectId, gradeLevel);
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
}
