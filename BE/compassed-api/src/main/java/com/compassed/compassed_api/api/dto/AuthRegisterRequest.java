package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AuthRegisterRequest {
    private String email;
    private String password;
    private String fullName;
}
