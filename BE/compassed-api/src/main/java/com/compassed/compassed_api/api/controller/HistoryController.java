package com.compassed.compassed_api.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PlacementHistoryResponse;
import com.compassed.compassed_api.local.LocalDataStore;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final LocalDataStore localDataStore;

    public HistoryController(LocalDataStore localDataStore) {
        this.localDataStore = localDataStore;
    }

    @GetMapping("/placements")
    public List<PlacementHistoryResponse> placementHistory(@RequestHeader("X-USER-ID") Long userId) {
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
