package com.compassed.compassed_api.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndSubjectId(Long userId, Long subjectId);
    boolean existsByUserIdAndSubjectIdAndIsActiveTrue(Long userId, Long subjectId);
    List<Subscription> findByUserIdAndIsActiveTrue(Long userId);
    long countByIsActiveTrue();
}
