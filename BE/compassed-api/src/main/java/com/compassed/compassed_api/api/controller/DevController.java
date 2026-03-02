package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
public class DevController {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DevController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Endpoint tạo admin user - CHỈ DÙNG ĐỂ DEV/TEST
     * POST /api/dev/create-admin
     * Body: {"email": "admin@example.com", "password": "admin123", "fullName": "Admin User"}
     */
    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");

        if (email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email và password là bắt buộc"));
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Check if user exists
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email đã tồn tại"));
        }

        // Create admin user
        User admin = new User();
        admin.setEmail(normalizedEmail);
        admin.setFullName(fullName != null ? fullName : "Admin");
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setOauthProvider("local");
        admin.setOauthProviderUserId("local_dev_" + System.currentTimeMillis());
        admin.setRole(UserRole.ADMIN); // SET ROLE = ADMIN

        admin = userRepository.save(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "✅ Đã tạo admin user thành công!");
        response.put("user", Map.of(
                "id", admin.getId(),
                "email", admin.getEmail(),
                "fullName", admin.getFullName(),
                "role", admin.getRole().name()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint set user làm admin
     * POST /api/dev/set-admin/{userId}
     */
    @PostMapping("/set-admin/{userId}")
    public ResponseEntity<Map<String, Object>> setUserAsAdmin(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User không tồn tại"));
        }

        user.setRole(UserRole.ADMIN);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "✅ Đã set user " + user.getEmail() + " làm ADMIN!");
        response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));

        return ResponseEntity.ok(response);
    }
}
