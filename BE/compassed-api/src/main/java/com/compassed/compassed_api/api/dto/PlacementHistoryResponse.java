package com.compassed.compassed_api.api.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PlacementHistoryResponse {
    private Long attemptId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Double scorePercent;
    private String level;
    private LocalDateTime submittedAt;
}
