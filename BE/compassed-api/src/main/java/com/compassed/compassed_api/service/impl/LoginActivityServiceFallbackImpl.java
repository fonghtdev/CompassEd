package com.compassed.compassed_api.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.service.LoginActivityService;

@Service
@Profile("!mysql")
public class LoginActivityServiceFallbackImpl implements LoginActivityService {
    @Override
    public void recordLogin(Long userId) {
        // no-op
    }

    @Override
    public int computeStreak(Long userId) {
        return 0;
    }
}
