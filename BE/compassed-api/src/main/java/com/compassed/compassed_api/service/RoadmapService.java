package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.FinalTestSubmitRequest;
import com.compassed.compassed_api.api.dto.LessonCompleteRequest;
import com.compassed.compassed_api.api.dto.MiniTestSubmitRequest;
import com.compassed.compassed_api.api.dto.RoadmapResponse;
import com.compassed.compassed_api.api.dto.UpLevelSubmitRequest;

public interface RoadmapService {
    RoadmapResponse getRoadmap(Long userId, Long subjectId);
    void completeLesson(Long userId, Long lessonId, LessonCompleteRequest request);
    void submitMiniTest(Long userId, Long subjectId, Long miniTestId, MiniTestSubmitRequest request);
    RoadmapResponse submitFinalTest(Long userId, Long subjectId, FinalTestSubmitRequest request);
    RoadmapResponse requestReplan(Long userId, Long subjectId);
    RoadmapResponse submitUpLevelTest(Long userId, Long subjectId, UpLevelSubmitRequest request);
    RoadmapResponse getLessonDetail(Long userId, Long lessonId);
}
