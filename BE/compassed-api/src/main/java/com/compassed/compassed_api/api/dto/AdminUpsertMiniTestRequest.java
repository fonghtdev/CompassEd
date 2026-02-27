package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AdminUpsertMiniTestRequest {
    private String title;
    private String questions;
    private String subject;
    private String level;
    private Integer lessonId;
}
