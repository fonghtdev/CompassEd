package com.compassed.compassed_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
    private Integer displayOrder;
    private Integer estimatedMinutes;
    private Boolean completed;
}
