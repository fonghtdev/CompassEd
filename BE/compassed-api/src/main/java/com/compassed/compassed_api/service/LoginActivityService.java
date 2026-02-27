package com.compassed.compassed_api.service;

public interface LoginActivityService {
    void recordLogin(Long userId);
    int computeStreak(Long userId);
}
