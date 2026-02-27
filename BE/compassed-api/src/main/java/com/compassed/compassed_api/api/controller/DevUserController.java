package com.compassed.compassed_api.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compassed.compassed_api.api.dto.DevCreateUserRequest;
import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.local.LocalDataStore;

@RestController
@Profile("local")
@RequestMapping("/api/dev")
public class DevUserController {

    private final LocalDataStore localDataStore;

    public DevUserController(LocalDataStore localDataStore) {
        this.localDataStore = localDataStore;
    }

    @PostMapping("/users")
    public User createUser(@RequestBody DevCreateUserRequest req) {
        return localDataStore.createUser(req.getEmail(), req.getFullName());
    }
}
