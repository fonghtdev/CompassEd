package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AuthLoginRequest {
    private String email;
    private String password;
}
