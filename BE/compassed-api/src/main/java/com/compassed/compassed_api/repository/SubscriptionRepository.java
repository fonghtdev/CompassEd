package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser_IdAndSubject_Id(Long userId, Long subjectId);
    boolean existsByUser_IdAndSubject_IdAndActiveTrue(Long userId, Long subjectId);
}
