package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AdminSendNotificationRequest {
    private Long userId;
    private boolean broadcast;
    private String title;
    private String message;
    private String type;
}
