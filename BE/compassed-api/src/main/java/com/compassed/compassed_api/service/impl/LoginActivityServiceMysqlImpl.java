package com.compassed.compassed_api.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.compassed.compassed_api.domain.entity.UserLoginActivity;
import com.compassed.compassed_api.repository.UserLoginActivityRepository;
import com.compassed.compassed_api.service.LoginActivityService;

@Service
@Profile("mysql")
public class LoginActivityServiceMysqlImpl implements LoginActivityService {

    private final UserLoginActivityRepository userLoginActivityRepository;

    public LoginActivityServiceMysqlImpl(UserLoginActivityRepository userLoginActivityRepository) {
        this.userLoginActivityRepository = userLoginActivityRepository;
    }

    @Override
    @Transactional
    public void recordLogin(Long userId) {
        if (userId == null) return;
        LocalDate today = LocalDate.now();
        if (userLoginActivityRepository.existsByUserIdAndLoginDate(userId, today)) {
            return;
        }
        UserLoginActivity row = new UserLoginActivity();
        row.setUserId(userId);
        row.setLoginDate(today);
        row.setCreatedAt(LocalDateTime.now());
        userLoginActivityRepository.save(row);
    }

    @Override
    @Transactional(readOnly = true)
    public int computeStreak(Long userId) {
        if (userId == null) return 0;
        List<UserLoginActivity> rows = userLoginActivityRepository.findByUserIdOrderByLoginDateDesc(userId);
        if (rows.isEmpty()) return 0;
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        for (UserLoginActivity row : rows) {
            if (row.getLoginDate() == null) continue;
            if (row.getLoginDate().isEqual(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (row.getLoginDate().isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }
}
