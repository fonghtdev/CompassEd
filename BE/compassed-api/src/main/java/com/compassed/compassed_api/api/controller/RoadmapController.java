package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.FinalTestSubmitRequest;
import com.compassed.compassed_api.api.dto.LessonCompleteRequest;
import com.compassed.compassed_api.api.dto.MiniTestSubmitRequest;
import com.compassed.compassed_api.api.dto.RoadmapResponse;
import com.compassed.compassed_api.security.CurrentUserService;
import com.compassed.compassed_api.service.RoadmapService;

@RestController
@RequestMapping("/api")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final CurrentUserService currentUserService;

    public RoadmapController(RoadmapService roadmapService, CurrentUserService currentUserService) {
        this.roadmapService = roadmapService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/subjects/{subjectId}/roadmap")
    public RoadmapResponse getRoadmap(
            @PathVariable Long subjectId
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return roadmapService.getRoadmap(userId, subjectId);
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public void completeLesson(
            @PathVariable Long lessonId,
            @RequestBody LessonCompleteRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        roadmapService.completeLesson(userId, lessonId, request);
    }

    @PostMapping("/subjects/{subjectId}/mini-tests/{miniTestId}/submit")
    public void submitMiniTest(
            @PathVariable Long subjectId,
            @PathVariable Long miniTestId,
            @RequestBody MiniTestSubmitRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        roadmapService.submitMiniTest(userId, subjectId, miniTestId, request);
    }

    @PostMapping("/subjects/{subjectId}/final-test/submit")
    public RoadmapResponse submitFinalTest(
            @PathVariable Long subjectId,
            @RequestBody FinalTestSubmitRequest request
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return roadmapService.submitFinalTest(userId, subjectId, request);
    }

    @GetMapping("/lessons/{lessonId}")
    public RoadmapResponse getLessonDetail(
            @PathVariable Long lessonId
    ) {
        Long userId = currentUserService.requireCurrentUserId();
        return roadmapService.getLessonDetail(userId, lessonId);
    }
}
