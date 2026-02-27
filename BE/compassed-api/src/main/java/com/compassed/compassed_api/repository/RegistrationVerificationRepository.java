package com.compassed.compassed_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.RegistrationVerification;

public interface RegistrationVerificationRepository extends JpaRepository<RegistrationVerification, Long> {
    Optional<RegistrationVerification> findByEmail(String email);
    void deleteByEmail(String email);
}
