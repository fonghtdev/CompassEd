package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AdminUpsertLessonRequest {
    private String title;
    private String content;
    private String subject;
    private String level;
    private Integer orderIndex;
}
