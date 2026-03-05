package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.PlacementAttempt;
import com.compassed.compassed_api.domain.enums.AttemptStatus;

public interface PlacementAttemptRepository extends JpaRepository<PlacementAttempt, Long> {
    Optional<PlacementAttempt> findByIdAndUser_Id(Long id, Long userId);
    Optional<PlacementAttempt> findTopByUser_IdOrderByStartedAtDesc(Long userId);
    Optional<PlacementAttempt> findTopByUser_IdAndSubject_IdAndStatusOrderByStartedAtDesc(Long userId, Long subjectId, AttemptStatus status);
}
