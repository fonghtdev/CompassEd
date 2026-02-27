package com.compassed.compassed_api.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.compassed.compassed_api.api.dto.AuthLoginRequest;
import com.compassed.compassed_api.api.dto.AuthMockOauthRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.api.dto.AuthUserDto;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.AuthService;

@Service
@Profile("jpa")
@Transactional
public class AuthServiceJpaImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${auth.google.client-id:}")
    private String googleClientId;

    public AuthServiceJpaImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthResponse register(AuthRegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        
        // Check if user already exists
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setOauthProvider("local");
        user.setOauthProviderUserId("local_" + UUID.randomUUID());
        
        user = userRepository.save(user);
        
        return authResponseForUser(user);
    }

    @Override
    public AuthResponse login(AuthLoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        
        return authResponseForUser(user);
    }

    @Override
    public AuthResponse loginWithGoogle(String idToken) {
        // For now, mock Google OAuth
        throw new RuntimeException("Google OAuth not implemented yet");
    }

    @Override
    public AuthResponse loginWithMockProvider(AuthMockOauthRequest request) {
        String provider = request.getProvider();
        String email = request.getEmail();
        String fullName = request.getFullName();
        
        if (provider == null || email == null) {
            throw new RuntimeException("Provider and email are required");
        }
        
        String normalizedEmail = email.trim().toLowerCase();
        
        // Check if user exists with this email and provider
        User user = userRepository.findByEmail(normalizedEmail).orElse(null);
        
        if (user == null) {
            // Create new OAuth user
            user = new User();
            user.setEmail(normalizedEmail);
            user.setFullName(fullName);
            user.setOauthProvider(provider);
            user.setOauthProviderUserId(provider + "_" + UUID.randomUUID());
            user = userRepository.save(user);
        }
        
        return authResponseForUser(user);
    }

    @Override
    public AuthUserDto me(String bearerToken) {
        // Simple token validation - in production use JWT
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }
        
        String token = bearerToken.substring(7);
        // For now, token is just the user ID
        try {
            Long userId = Long.parseLong(token);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            AuthUserDto dto = new AuthUserDto();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setFullName(user.getFullName());
            dto.setRole(user.getRole().name()); // Thêm role
            return dto;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid token format");
        }
    }

    private AuthResponse authResponseForUser(User user) {
        // In production, generate proper JWT token
        // Frontend will add "Bearer " prefix, so we only return the user ID
        String token = String.valueOf(user.getId());
        
        AuthUserDto userDto = new AuthUserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFullName(user.getFullName());
        userDto.setRole(user.getRole().name()); // Thêm role
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(userDto);
        
        return response;
    }
}
