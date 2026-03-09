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
    private String frameworkCode;
    private String frameworkTitle;
    private String frameworkDescription;
    private List<String> frameworkModules;
    private String roadmapGuideJson;
    private List<RoadmapModuleItem> roadmapModules;
    private List<SkillPlanItem> miniTestPlan;
    private List<SkillPlanItem> finalTestPlan;
    private List<QuestionItem> miniTestDraft;
    private List<QuestionItem> finalTestDraft;
    private Boolean roadmapInitialized;
    private Integer refreshCountUsed;
    private Integer refreshCountLimit;
    private Integer refreshCountRemaining;
    private Boolean canRefresh;

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

    @Data
    public static class SkillPlanItem {
        private String skillType;
        private Integer count;
    }

    @Data
    public static class RoadmapModuleItem {
        private Integer moduleNo;
        private String title;
        private List<String> focusSkills;
        private String studyGuide;
        private Integer targetScore;
        private String duration;
        private List<LessonPlanItem> lessonPlan;
    }

    @Data
    public static class LessonPlanItem {
        private Integer lessonNo;
        private String title;
        private String summary;
        private String duration;
    }
}
