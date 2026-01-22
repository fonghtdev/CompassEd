package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.UserRoadmapAssignment;

public interface UserRoadmapAssignmentRepository extends JpaRepository<UserRoadmapAssignment, Long> {
    Optional<UserRoadmapAssignment> findByUser_IdAndSubject_Id(Long userId, Long subjectId);
}
