package com.compassed.compassed_api.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class SubscribeResponse {
    private Integer totalSubjects;
    private Long totalAmountVnd; // 50k/90k/130k
    private List<SubscribeItemResponse> items;
}
