package com.compassed.compassed_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.compassed.compassed_api.domain.entity.MiniTest;

@Repository
public interface MiniTestRepository extends JpaRepository<MiniTest, Long> {
    List<MiniTest> findBySubjectAndLevelOrderByLessonIdAsc(String subject, String level);
    List<MiniTest> findByModuleId(Long moduleId);
}
