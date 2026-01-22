package com.compassed.compassed_api.service.impl;

import org.springframework.stereotype.Service;

import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.UserDevService;

@Service
public class UserDevServiceImpl implements UserDevService {

    private final UserRepository userRepository;

    public UserDevServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void ensureUserExists(Long userId) {
        if (userRepository.existsById(userId)) return;

        User u = new User();
        u.setId(userId); //IDENTITY không cho set id nếu auto increment
        //tạo user theo email thay vì set ID.
        throw new RuntimeException("Please create user via /api/dev/users first (V1)");
    }
}
