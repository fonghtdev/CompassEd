package com.compassed.compassed_api.repository;

import com.compassed.compassed_api.domain.entity.MiniTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MiniTestRepository extends JpaRepository<MiniTest, Long> {
    
    Optional<MiniTest> findByModuleId(Long moduleId);
}
