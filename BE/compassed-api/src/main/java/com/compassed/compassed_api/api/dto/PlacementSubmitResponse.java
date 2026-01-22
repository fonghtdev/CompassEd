package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class PlacementSubmitResponse {
    private Double scorePercent;
    private String level; // L1/L2/L3
    private String skillAnalysisJson;
    private String nextStep; // "Đăng ký để mở khóa roadmap"
}
