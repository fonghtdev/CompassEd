package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.UserRoleAssignment;

public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, Long> {
    Optional<UserRoleAssignment> findByUser_Id(Long userId);
}
