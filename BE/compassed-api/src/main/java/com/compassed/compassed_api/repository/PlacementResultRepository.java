package com.compassed.compassed_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.compassed.compassed_api.domain.entity.PlacementResult;

public interface PlacementResultRepository extends JpaRepository<PlacementResult, Long> {
    Optional<PlacementResult> findTopByUser_IdAndSubject_IdOrderByCreatedAtDesc(Long userId, Long subjectId);
    List<PlacementResult> findByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("select avg(p.scorePercent) from PlacementResult p")
    Double averageScorePercent();

    @Query("select count(p) from PlacementResult p where p.scorePercent >= 70")
    long countPassed();
}
