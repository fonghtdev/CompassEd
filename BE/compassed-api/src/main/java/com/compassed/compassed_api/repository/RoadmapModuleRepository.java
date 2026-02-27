package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.RoadmapModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapModuleRepository extends JpaRepository<RoadmapModule, Long> {
    
    List<RoadmapModule> findByRoadmapIdOrderByOrderIndexAsc(Long roadmapId);
    
    RoadmapModule findFirstByRoadmapIdAndOrderIndex(Long roadmapId, Integer orderIndex);
}
