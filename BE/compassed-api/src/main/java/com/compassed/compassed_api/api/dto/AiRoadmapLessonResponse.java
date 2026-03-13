package com.compassed.compassed_api.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class AiRoadmapLessonResponse {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String level;
    private String academicTrack;
    private Integer moduleNo;
    private String moduleTitle;
    private Integer lessonNo;
    private String lessonTitle;
    private String lessonSummary;
    private String duration;
    private List<String> learningObjectives;
    private List<LessonSectionItem> lessonSections;
    private List<String> practiceTasks;
    private List<String> keyTakeaways;
    private String reflectionPrompt;
    private String homework;

    @Data
    public static class LessonSectionItem {
        private String heading;
        private String body;
        private List<String> bullets;
    }
}
