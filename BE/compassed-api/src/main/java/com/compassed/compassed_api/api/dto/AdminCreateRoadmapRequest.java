package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AdminCreateRoadmapRequest {
    private Long subjectId;
    private String level;
    private String title;
    private String description;
    private Integer displayOrder;
}
