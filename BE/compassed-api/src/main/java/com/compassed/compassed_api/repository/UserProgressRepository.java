package com.compassed.compassed_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.compassed.compassed_api.domain.entity.UserProgress;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserIdAndSubject(Long userId, String subject);
    Optional<UserProgress> findByUserIdAndSubjectAndLevelAndLessonId(Long userId, String subject, String level, Long lessonId);
    List<UserProgress> findByUserIdAndSubjectAndLevel(Long userId, String subject, String level);
    void deleteByUserIdAndSubjectAndLevel(Long userId, String subject, String level);
}
