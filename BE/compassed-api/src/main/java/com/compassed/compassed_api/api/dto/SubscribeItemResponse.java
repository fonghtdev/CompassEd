package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class SubscribeItemResponse {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;

    // Nếu đã có placement => roadmap sẽ có
    // Nếu chưa có placement => roadmap null + status NEED_PLACEMENT
    private String status; // "ROADMAP_UNLOCKED" | "NEED_PLACEMENT"
    private String roadmapTitle;
    private String roadmapDescription;
}
