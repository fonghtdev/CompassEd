package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.MiniTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiniTestAttemptRepository extends JpaRepository<MiniTestAttempt, Long> {
    
    List<MiniTestAttempt> findByUserIdAndMiniTestIdOrderBySubmittedAtDesc(Long userId, Long miniTestId);
    
    List<MiniTestAttempt> findByUserIdOrderBySubmittedAtDesc(Long userId);
}
