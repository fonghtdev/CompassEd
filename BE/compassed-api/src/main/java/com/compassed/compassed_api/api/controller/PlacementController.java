package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.PlacementStartResponse;
import com.compassed.compassed_api.api.dto.PlacementSubmitRequest;
import com.compassed.compassed_api.api.dto.PlacementSubmitResponse;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.PlacementService;

@RestController
@RequestMapping("/api")
public class PlacementController {

    private final PlacementService placementService;
    private final CurrentUserService currentUserService;

    public PlacementController(PlacementService placementService, CurrentUserService currentUserService) {
        this.placementService = placementService;
        this.currentUserService = currentUserService;
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
    @PostMapping("/placement-attempts/{attemptId}/submit")
    public PlacementSubmitResponse submit(
            @PathVariable Long attemptId,
            @RequestBody PlacementSubmitRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return placementService.submitPlacement(userId, attemptId, request);
    }
}
