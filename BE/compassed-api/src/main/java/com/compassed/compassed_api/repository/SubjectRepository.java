package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
}
