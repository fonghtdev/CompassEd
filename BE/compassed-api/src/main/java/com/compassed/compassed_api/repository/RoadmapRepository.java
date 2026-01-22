package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.Roadmap;
import com.compassed.compassed_api.domain.enums.Level;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Optional<Roadmap> findBySubject_IdAndLevel(Long subjectId, Level level);
}
