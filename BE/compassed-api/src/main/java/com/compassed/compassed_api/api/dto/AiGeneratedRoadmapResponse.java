package com.compassed.compassed_api.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class AiGeneratedRoadmapResponse {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String level;
    private String academicTrack;
    private Double placementScorePercent;
    private String roadmapGuideJson;
    private List<QuestionItem> miniTestDraft;
    private List<QuestionItem> finalTestDraft;

    @Data
    public static class QuestionItem {
        private Long questionId;
        private String skillType;
        private String questionText;
        private String options;
        private String correctAnswer;
        private String explanation;
        private Integer difficulty;
    }
}
