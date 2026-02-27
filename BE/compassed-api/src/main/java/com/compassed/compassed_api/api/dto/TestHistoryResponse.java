package com.compassed.compassed_api.api.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TestHistoryResponse {
    private String testName;
    private String subjectCode;
    private String subjectName;
    private String level;
    private LocalDateTime submittedAt;
    private Double scorePercent;
}
