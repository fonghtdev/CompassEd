package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AuthRegisterVerifyRequest {
    private String email;
    private String code;
}
