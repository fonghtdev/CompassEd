package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AdminUpdateRoadmapRequest {
    private String title;
    private String description;
    private Integer displayOrder;
}
