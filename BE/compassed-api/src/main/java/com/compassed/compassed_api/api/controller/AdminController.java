package com.compassed.compassed_api.api.controller;

import com.compassed.compassed_api.config.RequireRole;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // ========== STATISTICS ==========
    
    @GetMapping("/stats")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSubjects", 3); // Fixed subjects: MATH, LITERATURE, ENGLISH
        stats.put("totalPlacementAttempts", 0);
        stats.put("totalSubscriptions", 0);
        
        return ResponseEntity.ok(stats);
    }
    
    // ========== USER MANAGEMENT ==========
    
    /**
     * Lấy danh sách tất cả users
     */
    @GetMapping("/users")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Xóa passwordHash trước khi trả về
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }
    
    /**
     * Lấy thông tin chi tiết 1 user
     */
    @GetMapping("/users/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                user.setPasswordHash(null);
                return ResponseEntity.ok(user);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Cập nhật thông tin user (email, fullName, role)
     */
    @PutMapping("/users/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return userRepository.findById(id)
            .map(user -> {
                if (updates.containsKey("email")) {
                    user.setEmail((String) updates.get("email"));
                }
                if (updates.containsKey("fullName")) {
                    user.setFullName((String) updates.get("fullName"));
                }
                if (updates.containsKey("role")) {
                    String roleStr = (String) updates.get("role");
                    user.setRole(UserRole.valueOf(roleStr.toUpperCase()));
                }
                
                User saved = userRepository.save(user);
                saved.setPasswordHash(null);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Đổi password cho user
     */
    @PutMapping("/users/{id}/password")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> changeUserPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password phải có ít nhất 6 ký tự"));
        }
        
        return userRepository.findById(id)
            .map(user -> {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Đổi password thành công"));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Xóa user (cẩn thận!)
     */
    @DeleteMapping("/users/{id}")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa user thành công"));
    }
    
    /**
     * Tạo user mới
     */
    @PostMapping("/users")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String fullName = body.get("fullName");
        String password = body.get("password");
        String roleStr = body.getOrDefault("role", "USER");
        
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email và password là bắt buộc"));
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email đã tồn tại"));
        }
        
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setProvider("local");
        user.setRole(UserRole.valueOf(roleStr.toUpperCase()));
        
        User saved = userRepository.save(user);
        saved.setPasswordHash(null);
        
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Toggle role giữa USER và ADMIN
     */
    @PostMapping("/users/{id}/toggle-admin")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<?> toggleAdminRole(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                if (user.getRole() == UserRole.ADMIN) {
                    user.setRole(UserRole.USER);
                } else {
                    user.setRole(UserRole.ADMIN);
                }
                
                User saved = userRepository.save(user);
                saved.setPasswordHash(null);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // ========== TEST ENDPOINT ==========
    
    @GetMapping("/test")
    @RequireRole(UserRole.ADMIN)
    public ResponseEntity<Map<String, String>> testAdminAccess() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ Bạn có quyền ADMIN!");
        response.put("access", "granted");
        return ResponseEntity.ok(response);
    }
}
