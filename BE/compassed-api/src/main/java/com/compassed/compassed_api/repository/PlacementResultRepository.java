package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.PlacementResult;

public interface PlacementResultRepository extends JpaRepository<PlacementResult, Long> {
    Optional<PlacementResult> findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(Long userId, Long subjectId);
}
