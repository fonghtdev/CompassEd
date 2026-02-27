package com.compassed.compassed_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.compassed.compassed_api.domain.entity.MiniTest;

@Repository
public interface MiniTestRepository extends JpaRepository<MiniTest, Long> {
    List<MiniTest> findBySubjectAndLevelOrderByLessonIdAsc(String subject, String level);
    Optional<MiniTest> findFirstByLessonId(Long lessonId);
}
