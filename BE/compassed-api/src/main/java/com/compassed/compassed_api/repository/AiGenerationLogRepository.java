package com.compassed.compassed_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.AiGenerationLog;

public interface AiGenerationLogRepository extends JpaRepository<AiGenerationLog, Long> {
    List<AiGenerationLog> findTop100ByOrderByCreatedAtDesc();
}
