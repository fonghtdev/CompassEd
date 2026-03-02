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
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.security.JwtTokenService;
import com.compassed.compassed_api.service.AuthService;
import com.compassed.compassed_api.service.LoginActivityService;
import com.compassed.compassed_api.service.RoleAccessService;

@Service
@Profile("mysql")
public class AuthServiceMysqlImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final RoleAccessService roleAccessService;
    private final LoginActivityService loginActivityService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final WebClient googleClient = WebClient.builder().baseUrl("https://oauth2.googleapis.com").build();

    @Value("${auth.google.client-id:}")
    private String googleClientId;

    public AuthServiceMysqlImpl(
            UserRepository userRepository,
            JwtTokenService jwtTokenService,
            RoleAccessService roleAccessService,
            LoginActivityService loginActivityService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.roleAccessService = roleAccessService;
        this.loginActivityService = loginActivityService;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        throw new RuntimeException("Direct register is disabled. Use /api/auth/register/request-code then /api/auth/register/verify");
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Email is not verified");
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
        String email = normalizeEmail(asString(tokenInfo.get("email")));
        String name = asString(tokenInfo.get("name"));
        if (sub == null || email.isBlank()) {
            throw new RuntimeException("Google token payload missing sub/email");
        }
        User user = upsertOAuthUser("google", sub, email, name);
        if (isAdmin(user)) {
            throw new RuntimeException("Admin account must sign in with email/password");
        }
        return authResponseForUser(user);
    }

    @Override
    public AuthUserDto me(String bearerToken) {
        String token = extractToken(bearerToken);
        Long userId = jwtTokenService.parseUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Invalid token"));
        return toUserDto(user);
    }

    private User upsertOAuthUser(String provider, String providerUserId, String email, String fullName) {
        return userRepository.findByOauthProviderAndOauthProviderUserId(provider, providerUserId)
                .or(() -> userRepository.findByEmail(email))
                .map(existing -> {
                    if (isAdmin(existing)) {
                        throw new RuntimeException("Admin account must sign in with email/password");
                    }
                    if (fullName != null && !fullName.isBlank()) {
                        existing.setFullName(fullName);
                    }
                    existing.setOauthProvider(provider);
                    existing.setOauthProviderUserId(providerUserId);
                    existing.setEmailVerified(true);
                    User saved = userRepository.save(existing);
                    if (saved.getRole() == null) {
                        roleAccessService.assignRole(saved, UserRole.USER);
                    }
                    return saved;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setFullName(fullName);
                    user.setOauthProvider(provider);
                    user.setOauthProviderUserId(providerUserId);
                    user.setEmailVerified(true);
                    User saved = userRepository.save(user);
                    roleAccessService.assignRole(saved, UserRole.USER);
                    return saved;
                });
    }

    private AuthResponse authResponseForUser(User user) {
        loginActivityService.recordLogin(user.getId());
        String role = roleAccessService.resolveRoleName(user);
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
        dto.setRole(roleAccessService.resolveRoleName(user));
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

    private String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(roleAccessService.resolveRoleName(user));
    }
}
