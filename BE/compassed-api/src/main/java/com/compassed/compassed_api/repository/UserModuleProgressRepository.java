package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.UserModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserModuleProgressRepository extends JpaRepository<UserModuleProgress, Long> {
    
    Optional<UserModuleProgress> findByUserIdAndModuleId(Long userId, Long moduleId);
    
    List<UserModuleProgress> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<UserModuleProgress> findByUserIdAndStatus(Long userId, String status);
}
