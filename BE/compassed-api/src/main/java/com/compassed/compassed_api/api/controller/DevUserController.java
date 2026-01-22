package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.DevCreateUserRequest;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.UserRepository;

@RestController
@RequestMapping("/api/dev")
public class DevUserController {

    private final UserRepository userRepository;

    public DevUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public User createUser(@RequestBody DevCreateUserRequest req) {
        User u = new User();
        u.setEmail(req.getEmail());
        u.setFullName(req.getFullName());
        return userRepository.save(u);
    }
}
