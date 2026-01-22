package com.compassed.compassed_api.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class SubscribeRequest {
    // User tick các môn muốn mua ở trang Payment
    // VD: [subjectIdToan, subjectIdAnh]
    private List<Long> subjectIds;
}
