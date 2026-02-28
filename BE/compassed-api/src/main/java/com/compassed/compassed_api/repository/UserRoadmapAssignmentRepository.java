package com.compassed.compassed_api.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.UserRoadmapAssignment;

public interface UserRoadmapAssignmentRepository extends JpaRepository<UserRoadmapAssignment, Long> {
    Optional<UserRoadmapAssignment> findByUserIdAndSubjectId(Long userId, Long subjectId);
    List<UserRoadmapAssignment> findByUser_Id(Long userId);
}
