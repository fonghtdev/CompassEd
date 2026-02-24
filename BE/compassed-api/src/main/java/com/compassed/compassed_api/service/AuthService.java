package com.compassed.compassed_api.service;

import com.compassed.compassed_api.api.dto.AuthLoginRequest;
import com.compassed.compassed_api.api.dto.AuthMockOauthRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.api.dto.AuthUserDto;

public interface AuthService {
    AuthResponse register(AuthRegisterRequest request);

    AuthResponse login(AuthLoginRequest request);

    AuthResponse loginWithGoogle(String idToken);

    AuthResponse loginWithMockProvider(AuthMockOauthRequest request);

    AuthUserDto me(String bearerToken);
}
