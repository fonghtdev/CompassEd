package com.compassed.compassed_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {}
