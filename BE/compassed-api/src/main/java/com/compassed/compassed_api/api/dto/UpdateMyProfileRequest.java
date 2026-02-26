package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class UpdateMyProfileRequest {
    private String fullName;
    private String learningGoal;
    private Integer targetScore;
    private Boolean notifyEmail;
    private Boolean notifyInApp;
}
