package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class ChangeMyPasswordRequest {
    private String currentPassword;
    private String newPassword;
}
