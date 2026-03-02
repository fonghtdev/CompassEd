package com.compassed.compassed_api.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.compassed.compassed_api.api.dto.AuthRegisterRequest;
import com.compassed.compassed_api.api.dto.AuthRegisterVerifyRequest;
import com.compassed.compassed_api.api.dto.AuthResponse;
import com.compassed.compassed_api.api.dto.AuthUserDto;
import com.compassed.compassed_api.domain.entity.RegistrationVerification;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.RegistrationVerificationRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.security.JwtTokenService;
import com.compassed.compassed_api.service.RegistrationVerificationService;
import com.compassed.compassed_api.service.RoleAccessService;

@Service
@Profile("mysql")
public class RegistrationVerificationServiceMysqlImpl implements RegistrationVerificationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationVerificationServiceMysqlImpl.class);
    private static final int CODE_EXPIRE_MINUTES = 10;

    private final UserRepository userRepository;
    private final RegistrationVerificationRepository verificationRepository;
    private final RoleAccessService roleAccessService;
    private final JwtTokenService jwtTokenService;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${auth.mail.from:no-reply@compassed.local}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public RegistrationVerificationServiceMysqlImpl(
            UserRepository userRepository,
            RegistrationVerificationRepository verificationRepository,
            RoleAccessService roleAccessService,
            JwtTokenService jwtTokenService,
            JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.roleAccessService = roleAccessService;
        this.jwtTokenService = jwtTokenService;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public Map<String, Object> requestCode(AuthRegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String code = generateCode();
        RegistrationVerification pending = verificationRepository.findByEmail(email)
                .orElseGet(RegistrationVerification::new);
        pending.setEmail(email);
        pending.setFullName(request.getFullName());
        pending.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        pending.setVerificationCode(code);
        pending.setCreatedAt(LocalDateTime.now());
        pending.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        verificationRepository.save(pending);

        sendVerificationEmail(email, code, request.getFullName());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Verification code sent to email");
        response.put("expiresInMinutes", CODE_EXPIRE_MINUTES);
        return response;
    }

    @Override
    @Transactional
    public AuthResponse verifyCode(AuthRegisterVerifyRequest request) {
        String email = normalizeEmail(request.getEmail());
        String code = normalizeCode(request.getCode());
        if (email.isBlank() || code.isBlank()) {
            throw new RuntimeException("Email and code are required");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            verificationRepository.deleteByEmail(email);
            throw new RuntimeException("Email already exists");
        }

        RegistrationVerification pending = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Verification request not found"));
        if (pending.getExpiresAt() == null || pending.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code expired");
        }
        if (!code.equals(pending.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(pending.getFullName());
        user.setPasswordHash(pending.getPasswordHash());
        user.setEmailVerified(true);
        user = userRepository.save(user);
        roleAccessService.assignRole(user, UserRole.USER);
        verificationRepository.deleteByEmail(email);
        return authResponseForUser(user);
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
                    + "\nThis code expires in " + CODE_EXPIRE_MINUTES + " minutes.\n\nCompassED Team");
            mailSender.send(message);
            log.info("Verification email sent successfully to {}", email);
        } catch (Exception ex) {
            // Fallback: log the code to console if email sending fails
            log.warn("==============================================================");
            log.warn("Cannot send email to {} - Error: {}", email, ex.getMessage());
            log.warn("VERIFICATION CODE for {}: {}", email, code);
            log.warn("Use this code to complete registration (expires in {} minutes)", CODE_EXPIRE_MINUTES);
            log.warn("==============================================================");
            // Do NOT throw exception - allow registration to continue with manual code entry
        }
    }

    private AuthResponse authResponseForUser(User user) {
        String role = roleAccessService.resolveRoleName(user);
        String token = jwtTokenService.generateToken(user.getId(), user.getEmail(), role);

        AuthUserDto dto = new AuthUserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(role);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUser(dto);
        return response;
    }

    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
