package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.FinalTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalTestRepository extends JpaRepository<FinalTest, Long> {
    
    Optional<FinalTest> findByRoadmapId(Long roadmapId);
}
