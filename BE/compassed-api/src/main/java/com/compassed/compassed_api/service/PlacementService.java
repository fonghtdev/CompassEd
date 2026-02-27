package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.PlacementStartResponse;
import com.compassed.compassed_api.api.dto.PlacementSubmitRequest;
import com.compassed.compassed_api.api.dto.PlacementSubmitResponse;

public interface PlacementService {
    PlacementStartResponse startPlacement(Long userId, Long subjectId);
    PlacementSubmitResponse submitPlacement(Long userId, Long attemptId, PlacementSubmitRequest request);
    int checkFreeAttempts(Long userId, Long subjectId);
    void decrementFreeAttempts(Long userId, Long subjectId);
}
