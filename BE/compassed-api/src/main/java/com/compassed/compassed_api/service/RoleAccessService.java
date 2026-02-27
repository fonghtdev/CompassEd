package com.compassed.compassed_api.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.compassed.compassed_api.domain.entity.AppRole;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.domain.entity.UserRoleAssignment;
import com.compassed.compassed_api.domain.enums.UserRole;
import com.compassed.compassed_api.repository.AppRoleRepository;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.repository.UserRoleAssignmentRepository;

@Service
@Profile("mysql")
public class RoleAccessService {

    private final UserRepository userRepository;
    private final AppRoleRepository appRoleRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;

    public RoleAccessService(
            UserRepository userRepository,
            AppRoleRepository appRoleRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository) {
        this.userRepository = userRepository;
        this.appRoleRepository = appRoleRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
    }

    @Transactional
    public String resolveRoleName(User user) {
        if (user == null || user.getId() == null) {
            return UserRole.USER.name();
        }
        var mapped = userRoleAssignmentRepository.findByUser_Id(user.getId())
                .map(m -> m.getRole().getName())
                .orElse(null);
        if (mapped != null && !mapped.isBlank()) {
            return mapped.trim().toUpperCase();
        }
        if (user.getRole() != null) {
            return user.getRole().name();
        }
        return UserRole.USER.name();
    }

    @Transactional
    public void assignRole(User user, UserRole role) {
        if (user == null || user.getId() == null) {
            throw new RuntimeException("User not found");
        }
        UserRole normalizedRole = role == null ? UserRole.USER : role;

        user.setRole(normalizedRole);
        userRepository.save(user);

        AppRole dbRole = appRoleRepository.findByName(normalizedRole.name())
                .orElseGet(() -> {
                    AppRole created = new AppRole();
                    created.setName(normalizedRole.name());
                    return appRoleRepository.save(created);
                });

        UserRoleAssignment assignment = userRoleAssignmentRepository.findByUser_Id(user.getId())
                .orElseGet(UserRoleAssignment::new);
        assignment.setUser(user);
        assignment.setRole(dbRole);
        userRoleAssignmentRepository.save(assignment);
    }
}
