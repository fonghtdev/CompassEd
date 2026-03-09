package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.UserAiRoadmapState;

public interface UserAiRoadmapStateRepository extends JpaRepository<UserAiRoadmapState, Long> {
    Optional<UserAiRoadmapState> findByUser_IdAndSubject_Id(Long userId, Long subjectId);
}

