package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.FinalTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinalTestAttemptRepository extends JpaRepository<FinalTestAttempt, Long> {
    
    List<FinalTestAttempt> findByUserIdAndFinalTestIdOrderBySubmittedAtDesc(Long userId, Long finalTestId);
    
    List<FinalTestAttempt> findByUserIdOrderBySubmittedAtDesc(Long userId);
}
