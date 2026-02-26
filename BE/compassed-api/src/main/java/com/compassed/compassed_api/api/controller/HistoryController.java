package com.compassed.compassed_api.api.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PlacementHistoryResponse;
import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.security.CurrentUserService;

@RestController
@Profile("local")
@RequestMapping("/api/history")
public class HistoryController {

    private final LocalDataStore localDataStore;
    private final CurrentUserService currentUserService;

    public HistoryController(LocalDataStore localDataStore, CurrentUserService currentUserService) {
        this.localDataStore = localDataStore;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/placements")
    public List<PlacementHistoryResponse> placementHistory() {
        Long userId = currentUserService.requireCurrentUserId();
        return localDataStore.getPlacementHistory(userId).stream().map(item -> {
            PlacementHistoryResponse response = new PlacementHistoryResponse();
            response.setAttemptId(item.attemptId());
            response.setSubjectId(item.subjectId());
            response.setSubjectCode(item.subjectCode());
            response.setSubjectName(item.subjectName());
            response.setScorePercent(item.scorePercent());
            response.setLevel(item.level());
            response.setSubmittedAt(item.submittedAt());
            return response;
        }).toList();
    }
}
