package com.compassed.compassed_api.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.compassed.compassed_api.api.dto.AuthLoginRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.api.dto.AuthUserDto;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.security.JwtTokenService;
import com.compassed.compassed_api.service.AuthService;

@Service
@Profile("removed-local")
public class AuthServiceLocalImpl implements AuthService {

    private final LocalDataStore localDataStore;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final WebClient googleClient = WebClient.builder().baseUrl("https://oauth2.googleapis.com").build();

    @Value("${auth.google.client-id:}")
    private String googleClientId;

    public AuthServiceLocalImpl(LocalDataStore localDataStore, JwtTokenService jwtTokenService) {
        this.localDataStore = localDataStore;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        String hash = passwordEncoder.encode(request.getPassword());
        User user = localDataStore.createLocalAuthUser(request.getEmail(), request.getFullName(), hash);
        return authResponseForUser(user);
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        User user = localDataStore.findUserByEmail(request.getEmail());
        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }
        String hash = localDataStore.getPasswordHash(user.getId());
        if (hash == null || !passwordEncoder.matches(request.getPassword(), hash)) {
            throw new RuntimeException("Invalid email or password");
        }
        return authResponseForUser(user);
    }

    @Override
    public AuthResponse loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Google idToken is required");
        }

        Map<String, Object> tokenInfo = googleClient.get()
                .uri(uriBuilder -> uriBuilder.path("/tokeninfo").queryParam("id_token", idToken).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (tokenInfo == null) {
            throw new RuntimeException("Google token verification failed");
        }

        String aud = asString(tokenInfo.get("aud"));
        if (googleClientId != null && !googleClientId.isBlank() && !googleClientId.equals(aud)) {
            throw new RuntimeException("Google token audience mismatch");
        }

        String sub = asString(tokenInfo.get("sub"));
        String email = asString(tokenInfo.get("email"));
        String name = asString(tokenInfo.get("name"));
        if (sub == null || email == null) {
            throw new RuntimeException("Google token payload missing sub/email");
        }

        User user = localDataStore.upsertOAuthUser("google", sub, email, name);
        return authResponseForUser(user);
    }

    @Override
    public AuthUserDto me(String bearerToken) {
        Long userId = jwtTokenService.parseUserId(extractToken(bearerToken));
        User user = localDataStore.getUser(userId);
        if (user == null) {
            throw new RuntimeException("Invalid token");
        }
        return toUserDto(user);
    }

    private AuthResponse authResponseForUser(User user) {
        String role = user.getRole() == null ? UserRole.USER.name() : user.getRole().name();
        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), role);
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(toUserDto(user));
        return response;
    }

    private AuthUserDto toUserDto(User user) {
        AuthUserDto dto = new AuthUserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole() == null ? UserRole.USER.name() : user.getRole().name());
        return dto;
    }

    private String extractToken(String bearerToken) {
        if (bearerToken == null) {
            return "";
        }
        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return bearerToken.trim();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

