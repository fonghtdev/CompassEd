package com.compassed.compassed_api.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapResponse {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Boolean subscribed;
    private Boolean placementReady;
    private String level;
    private List<LessonResponse> lessons;
    private List<MiniTestResponse> miniTests;
    private Integer finalTestScore;
    private Integer replanCount;
    private Double miniTestAverageScore;
    private Integer progressPercent;
    private String phase;
    private String nextStep;
}
