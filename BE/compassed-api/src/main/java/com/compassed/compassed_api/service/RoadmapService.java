package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.FinalTestSubmitRequest;
import com.compassed.compassed_api.api.dto.LessonCompleteRequest;
import com.compassed.compassed_api.api.dto.MiniTestSubmitRequest;
import com.compassed.compassed_api.api.dto.RoadmapResponse;

import com.compassed.compassed_api.domain.entity.RoadmapModule;
import com.compassed.compassed_api.domain.entity.UserModuleProgress;
import java.util.List;

public interface RoadmapService {
    RoadmapResponse getRoadmap(Long userId, Long subjectId);
    void completeLesson(Long userId, Long lessonId, LessonCompleteRequest request);
    void submitMiniTest(Long userId, Long subjectId, Long miniTestId, MiniTestSubmitRequest request);
    RoadmapResponse submitFinalTest(Long userId, Long subjectId, FinalTestSubmitRequest request);
    RoadmapResponse getLessonDetail(Long userId, Long lessonId);

    // Bổ sung stub cho các method bị thiếu
    RoadmapModule getModule(Long moduleId);
    UserModuleProgress startModule(Long userId, Long moduleId);
    void completeModule(Long userId, Long moduleId, Integer miniTestScore);
    List<UserModuleProgress> getUserProgress(Long userId);
}
