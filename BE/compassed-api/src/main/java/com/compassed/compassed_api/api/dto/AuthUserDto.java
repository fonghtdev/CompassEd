package com.compassed.compassed_api.api.dto;

import lombok.Data;

@Data
public class AuthUserDto {
    private Long id;
    private String email;
    private String fullName;
}
