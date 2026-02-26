package com.compassed.compassed_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiniTestResponse {
    private Long id;
    private String title;
    private Integer questionCount;
    private Integer lessonId;
    private Boolean completed;
    private Integer score;
}
