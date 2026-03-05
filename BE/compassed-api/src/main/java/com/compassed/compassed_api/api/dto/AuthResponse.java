package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private AuthUserDto user;
    private Boolean placementOnboardingRequired;
}
