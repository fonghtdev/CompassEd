package com.compassed.compassed_api.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.UserDevService;

@Service
@Profile("mysql")
public class UserDevServiceImpl implements UserDevService {

    private final UserRepository userRepository;

    public UserDevServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found. Please register/login first.");
        }
    }
}
