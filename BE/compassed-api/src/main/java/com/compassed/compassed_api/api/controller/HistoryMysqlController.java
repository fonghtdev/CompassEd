package com.compassed.compassed_api.api.controller;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PlacementHistoryResponse;
import com.compassed.compassed_api.repository.PlacementResultRepository;
import com.compassed.compassed_api.security.CurrentUserService;

@RestController
@Profile("mysql")
@RequestMapping("/api/history")
public class HistoryMysqlController {

    private final PlacementResultRepository placementResultRepository;
    private final CurrentUserService currentUserService;

    public HistoryMysqlController(
            PlacementResultRepository placementResultRepository,
            CurrentUserService currentUserService) {
        this.placementResultRepository = placementResultRepository;
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
}
