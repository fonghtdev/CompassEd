package com.compassed.compassed_api.api.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.AuthGoogleRequest;
import com.compassed.compassed_api.api.dto.AuthLoginRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterVerifyRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.api.dto.AuthUserDto;
import com.compassed.compassed_api.service.AuthService;
import com.compassed.compassed_api.service.RegistrationVerificationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RegistrationVerificationService registrationVerificationService;

    public AuthController(
            AuthService authService,
            RegistrationVerificationService registrationVerificationService) {
        this.authService = authService;
        this.registrationVerificationService = registrationVerificationService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody AuthRegisterRequest request) {
        return registrationVerificationService.requestCode(request);
    }

    @PostMapping("/register/request-code")
    public Map<String, Object> requestRegisterCode(@RequestBody AuthRegisterRequest request) {
        return registrationVerificationService.requestCode(request);
    }

    @PostMapping("/register/verify")
    public AuthResponse verifyRegisterCode(@RequestBody AuthRegisterVerifyRequest request) {
        return registrationVerificationService.verifyCode(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/oauth/google")
    public AuthResponse loginWithGoogle(@RequestBody AuthGoogleRequest request) {
        return authService.loginWithGoogle(request.getIdToken());
    }

    @GetMapping("/me")
    public AuthUserDto me(@RequestHeader("Authorization") String authorization) {
        return authService.me(authorization);
    }
}
