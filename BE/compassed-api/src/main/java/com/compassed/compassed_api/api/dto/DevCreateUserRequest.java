package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class DevCreateUserRequest {
    private String email;
    private String fullName;
}
