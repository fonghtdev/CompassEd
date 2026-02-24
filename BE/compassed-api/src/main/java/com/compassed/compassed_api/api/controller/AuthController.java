package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.AuthGoogleRequest;
import com.compassed.compassed_api.api.dto.AuthLoginRequest;
import com.compassed.compassed_api.api.dto.AuthMockOauthRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.api.dto.AuthUserDto;
import com.compassed.compassed_api.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/oauth/google")
    public AuthResponse loginWithGoogle(@RequestBody AuthGoogleRequest request) {
        return authService.loginWithGoogle(request.getIdToken());
    }

    @PostMapping("/oauth/mock")
    public AuthResponse loginWithMockProvider(@RequestBody AuthMockOauthRequest request) {
        return authService.loginWithMockProvider(request);
    }

    @GetMapping("/me")
    public AuthUserDto me(@RequestHeader("Authorization") String authorization) {
        return authService.me(authorization);
    }
}
