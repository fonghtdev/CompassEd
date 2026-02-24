package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AuthMockOauthRequest {
    private String provider;
    private String email;
    private String fullName;
}
