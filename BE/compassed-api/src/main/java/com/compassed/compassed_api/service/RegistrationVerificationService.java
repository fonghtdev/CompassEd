package com.compassed.compassed_api.service;

import java.util.Map;

import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterVerifyRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;

public interface RegistrationVerificationService {
    Map<String, Object> requestCode(AuthRegisterRequest request);
    AuthResponse verifyCode(AuthRegisterVerifyRequest request);
}
