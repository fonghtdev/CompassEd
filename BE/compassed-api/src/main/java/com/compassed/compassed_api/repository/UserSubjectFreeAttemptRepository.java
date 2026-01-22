package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.UserSubjectFreeAttempt;

public interface UserSubjectFreeAttemptRepository extends JpaRepository<UserSubjectFreeAttempt, Long> {
    Optional<UserSubjectFreeAttempt> findByUser_IdAndSubject_Id(Long userId, Long subjectId);
}
