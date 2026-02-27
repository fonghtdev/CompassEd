package com.compassed.compassed_api.config;

import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public RoleInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Chỉ check với controller method có annotation @RequireRole
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // Nếu không có annotation @RequireRole, cho phép truy cập
        if (requireRole == null) {
            return true;
        }

        // Lấy token từ header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized - Missing token\"}");
            return false;
        }

        String token = authHeader.substring(7);
        
        try {
            // Parse userId từ token
            Long userId = Long.parseLong(token);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Unauthorized - User not found\"}");
                return false;
            }

            // Check role
            UserRole userRole = user.getRole();
            UserRole[] allowedRoles = requireRole.value();

            boolean hasPermission = Arrays.asList(allowedRoles).contains(userRole);

            if (!hasPermission) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"Forbidden - You don't have permission to access this resource\"}");
                return false;
            }

            // Lưu user vào request attribute để controller có thể dùng
            request.setAttribute("currentUser", user);
            return true;

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid token\"}");
            return false;
        }
    }
}
