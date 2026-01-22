package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class PlacementSubmitRequest {
    // FE gửi lại JSON answers theo format bạn tự định nghĩa.
    // V1 đơn giản: answersJson là string JSON
    private String answersJson;
}
