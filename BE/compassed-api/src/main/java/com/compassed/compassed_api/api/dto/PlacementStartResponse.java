package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class PlacementStartResponse {
    private Long attemptId;
    private Long subjectId;
    private String paperJson;
    private String answersJson;
}
