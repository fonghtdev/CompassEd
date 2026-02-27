package com.compassed.compassed_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompleteRequest {
    private Long subjectId;
    private Integer timeSpentSeconds;
    private Integer score;
}
