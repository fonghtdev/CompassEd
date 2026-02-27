package com.compassed.compassed_api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.compassed.compassed_api.domain.entity.UserLoginActivity;

public interface UserLoginActivityRepository extends JpaRepository<UserLoginActivity, Long> {
    boolean existsByUserIdAndLoginDate(Long userId, LocalDate loginDate);
    List<UserLoginActivity> findByUserIdOrderByLoginDateDesc(Long userId);
}
