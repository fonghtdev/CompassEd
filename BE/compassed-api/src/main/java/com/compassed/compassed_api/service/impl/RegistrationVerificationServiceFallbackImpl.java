package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterVerifyRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.service.AuthService;
import com.compassed.compassed_api.service.RegistrationVerificationService;

@Service
@Profile("!mysql")
public class RegistrationVerificationServiceFallbackImpl implements RegistrationVerificationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationVerificationServiceFallbackImpl.class);
    private final AuthService authService;
    private final JavaMailSender mailSender;
    private final Map<String, PendingRegister> pendingRegisters = new ConcurrentHashMap<>();

    @Value("${auth.mail.from:no-reply@compassed.local}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public RegistrationVerificationServiceFallbackImpl(AuthService authService, JavaMailSender mailSender) {
        this.authService = authService;
        this.mailSender = mailSender;
    }

    @Override
    public Map<String, Object> requestCode(AuthRegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        pendingRegisters.put(email, new PendingRegister(request, code, LocalDateTime.now().plusMinutes(10)));
        sendVerificationEmail(email, code, request.getFullName());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Verification code sent to email");
        response.put("expiresInMinutes", 10);
        return response;
    }

    @Override
    public AuthResponse verifyCode(AuthRegisterVerifyRequest request) {
        String email = normalizeEmail(request.getEmail());
        String code = request.getCode() == null ? "" : request.getCode().trim();
        PendingRegister pending = pendingRegisters.get(email);
        if (pending == null) {
            throw new RuntimeException("Verification request not found");
        }
        if (pending.expiresAt().isBefore(LocalDateTime.now())) {
            pendingRegisters.remove(email);
            throw new RuntimeException("Verification code expired");
        }
        if (!pending.code().equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }
        pendingRegisters.remove(email);
        return authService.register(pending.request());
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private void sendVerificationEmail(String email, String code, String fullName) {
        // If SMTP credentials are not configured, log the code to console instead of sending email
        if (mailUsername == null || mailUsername.isBlank()) {
            log.warn("==============================================================");
            log.warn("MAIL NOT CONFIGURED - Verification code for {}: {}", email, code);
            log.warn("==============================================================");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("CompassED - Verification code");
            String name = (fullName == null || fullName.isBlank()) ? "there" : fullName.trim();
            message.setText("Hi " + name + ",\n\nYour CompassED verification code is: " + code
                    + "\nThis code expires in 10 minutes.\n\nCompassED Team");
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Cannot send verification email to {}. Code={}. Error={}", email, ex.getMessage(), code);
            log.warn("Use the code above to verify manually during development.");
        }
    }

    private record PendingRegister(AuthRegisterRequest request, String code, LocalDateTime expiresAt) {
    }
}
