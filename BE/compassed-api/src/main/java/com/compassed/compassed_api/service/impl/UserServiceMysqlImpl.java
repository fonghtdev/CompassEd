package com.compassed.compassed_api.service.impl;

import com.compassed.compassed_api.domain.entity.User;
import com.compassed.compassed_api.repository.UserRepository;
import com.compassed.compassed_api.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Profile("mysql")
public class UserServiceMysqlImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceMysqlImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
