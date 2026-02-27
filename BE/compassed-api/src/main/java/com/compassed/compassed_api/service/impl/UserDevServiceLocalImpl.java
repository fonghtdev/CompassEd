package com.compassed.compassed_api.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.compassed.compassed_api.local.LocalDataStore;
import com.compassed.compassed_api.service.UserDevService;

@Service
@Profile("local")
public class UserDevServiceLocalImpl implements UserDevService {

    private final LocalDataStore localDataStore;

    public UserDevServiceLocalImpl(LocalDataStore localDataStore) {
        this.localDataStore = localDataStore;
    }

    @Override
    public void ensureUserExists(Long userId) {
        if (!localDataStore.userExists(userId)) {
            throw new RuntimeException("Please create user via /api/dev/users first (local mode)");
        }
    }
}
